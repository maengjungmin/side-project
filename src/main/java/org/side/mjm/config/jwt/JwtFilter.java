package org.side.mjm.config.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.side.mjm.common.contextHolder.ApplicationContextHolder;
import org.side.mjm.common.exception.code.CommonErrorCode;
import org.side.mjm.common.variable.CommonVariables;
import org.side.mjm.config.jwt.record.JwtValidDto;
import org.side.mjm.domain.user.UserRepository;
import org.side.mjm.entity.m_user;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.side.mjm.config.jwt.TokenProvider.AUTHORIZATION_FAIL_TYPE;


@RequiredArgsConstructor
public class JwtFilter extends GenericFilterBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(JwtFilter.class);
    private final List<String> ignoreUris = List.of(CommonVariables.IGNORE_URIS);
    private final TokenProvider tokenProvider;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        String requestURI = httpServletRequest.getRequestURI().replace(CommonVariables.CONTEXT_PATH, "");
        if (!ignoreUris.contains(requestURI) && !requestURI.startsWith("/swagger-") && !requestURI.startsWith("/api-docs")) {
            LOGGER.info("Request URI : '{}', Start to check access token. ▼", requestURI);
            String accessToken = null;
            /*1. Cookie 에서 Access Token 추출 (AUT)*/
            accessToken = tokenProvider.getTokenFromCookie(httpServletRequest);
            JwtValidDto valid = new JwtValidDto(false, null, accessToken);
            /*2. Access Token 유효성 체크*/
            if (StringUtils.hasText(accessToken)) {
                checkTokenValidity(valid, httpServletRequest, httpServletResponse);
            }
            /*3. 중복 로그인인지 체크*/
            if (valid.isValid()) checkDuplicationLogin(valid, httpServletRequest);
            /*4. Spring security 에 권한 정보 저장
             * 권한 정보가 없을 경우 JwtAuthenticationEntryPoint 로 전달.(security config 에 설정)*/
            if (valid.isValid()) {
                LOGGER.info("User's token validation success: '{}'", valid.getUserId());
                Authentication authentication = tokenProvider.getAuthentication(valid.getAccessToken());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        chain.doFilter(request, response);
    }

    private void checkTokenValidity(JwtValidDto valid,
                                    HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        if (!StringUtils.hasText(valid.getAccessToken()) || !tokenProvider.validateToken(valid.getAccessToken())) {
            valid.setValid(false);
            String refreshToken = null;
            /*Access Token 이 만료 되었을 경우 RefreshToken 을 확인*/
            try {
                refreshToken = tokenProvider.getTokenFromRequest(httpServletRequest);
                if (StringUtils.hasText(refreshToken) && tokenProvider.validateToken(refreshToken)) {
                    /*refreshToken 에서 권힌 정보를 추출해 새로운 Access Token 생성*/
                    Authentication authentication = tokenProvider.getAuthentication(refreshToken);
                    String userId = tokenProvider.getUid(refreshToken);
                    valid.setUserId(userId);
                    String newAccessToken = tokenProvider.createAccessToken(authentication);
                    /*쿠키 Access Token 정보 갱신*/
                    tokenProvider.renewalAccessTokenInCookie(httpServletResponse, newAccessToken);
                    /*Refresh Token 으로 User 를 조회 해  Access Token 갱신*/
                    UserRepository userRepository = ApplicationContextHolder.getContext().getBean(UserRepository.class);
                    Optional<m_user> optionalEntity = userRepository.findOneByUserIdAndRefreshToken(valid.getUserId(), refreshToken);
                    if (optionalEntity.isPresent()) {
                        optionalEntity.get().setAccessToken(newAccessToken);
                        userRepository.save(optionalEntity.get());
                        valid.setAccessToken(newAccessToken);
                        valid.setValid(true);
                        LOGGER.info("Renew user's access token with refresh token: '{}'", valid.getUserId());
                    } else {
                        LOGGER.error("User's refresh token is different. (Duplicated login): '{}'", valid.getUserId());
                    }
                }
            } catch (NullPointerException e) {
                LOGGER.error("Refresh token extraction failed.");
            }
        } else {
            String userId = tokenProvider.getUid(valid.getAccessToken());
            valid.setValid(true);
            valid.setUserId(userId);
        }
    }

    private void checkDuplicationLogin(JwtValidDto valid, HttpServletRequest httpServletRequest) {
        UserRepository userRepository = ApplicationContextHolder.getContext().getBean(UserRepository.class);
        Optional<m_user> optionalEntity = userRepository.findOneByUserIdAndAccessToken(valid.getUserId(), valid.getAccessToken());
        if (optionalEntity.isEmpty()) {
            LOGGER.error("User's access token is different. (Duplicated login): '{}'", valid.getUserId());
            valid.setValid(false);
            httpServletRequest.getSession().setAttribute(AUTHORIZATION_FAIL_TYPE, CommonErrorCode.DUPLICATION_LOGIN);
        }
    }
}