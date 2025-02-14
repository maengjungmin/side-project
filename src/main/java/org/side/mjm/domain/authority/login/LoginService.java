package org.side.mjm.domain.authority.login;

import io.jsonwebtoken.JwtException;
import jakarta.persistence.PersistenceException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import org.side.mjm.common.exception.code.CommonErrorCode;
import org.side.mjm.common.exception.custom.ServiceException;
import org.side.mjm.common.exception.custom.UnauthorizedException;
import org.side.mjm.common.response.structure.ItemResponse;
import org.side.mjm.config.jwt.TokenProvider;
import org.side.mjm.config.jwt.record.TokenResponse;
import org.side.mjm.config.message.MessageConfig;
import org.side.mjm.config.rsa.RsaProvider;
import org.side.mjm.domain.authority.login.record.LoginRequest;
import org.side.mjm.domain.authority.login.record.LogoutResponse;
import org.side.mjm.domain.loginHistory.LoginHistoryRepository;
import org.side.mjm.domain.user.UserRepository;
import org.side.mjm.entity.key.l_login_key;
import org.side.mjm.entity.l_login;
import org.side.mjm.entity.m_user;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LoginService {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoginService.class);
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final UserRepository userRepository;
    private final RsaProvider rsaProvider;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private final MessageConfig messageConfig;
//    private final LoginHistoryService loginHistoryService;
    private final LoginHistoryRepository loginHistoryRepository;

    @Value("${password.cycle}")
    private int passwordUpdateCycle;

    @Value("${password.locked}")
    private int locked;

    @Transactional
    public ResponseEntity<ItemResponse<TokenResponse>> login(
            LoginRequest parameter, HttpServletResponse httpServletResponse) {
        /*1. ID가 존재하는지 체크*/
        m_user user = userRepository.findById(parameter.id()).orElse(null);
        if(user == null) {
            throw new UnauthorizedException(CommonErrorCode.NO_MATCHING_USER, null);
        }

        /*2. 잠긴 회원 체크 (count == 5)*/
        if (!Objects.isNull(user.getLoginErrorCount()) && user.getLoginErrorCount() == this.locked) {
            throw new ServiceException(CommonErrorCode.LOCKED_MEMBER, "'" + user.getUserId() + "' is a locked member.");
        }

        /*3. Password 가 일치하는지 체크*/
        String encodePassword = "";
        try {
            encodePassword = rsaProvider.decrypt(parameter.password());
            if (!passwordEncoder.matches(encodePassword, user.getPassword())) {
                throw new ServiceException(CommonErrorCode.WRONG_PASSWORD);
            }
        } catch (ServiceException e) {
            user.setLoginErrorCount((user.getLoginErrorCount() == null) ? 1 : user.getLoginErrorCount() + 1);
            userRepository.save(user);
            throw new ServiceException(CommonErrorCode.WRONG_PASSWORD);
        }
        LOGGER.info("Correct password: {}", parameter.id());

        /*4. Password 갱신 주기 체크*/
        boolean isChangePassword = true;
        if (!Objects.isNull(user.getPasswordUpdateDate())) {
            isChangePassword = LocalDate.now().isAfter(user.getPasswordUpdateDate().plusDays(this.passwordUpdateCycle));
        }

        /*5. 사용자 권한 체크*/
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                parameter.id(), encodePassword);
        Authentication authentication = authenticationManagerBuilder.getObject()
                .authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        /*6. JWT 토큰 생성*/
        TokenResponse tokenResponse = tokenProvider.createToken(authentication, isChangePassword, user);

        /* 로그인 시간 추가 */
        LocalDateTime currentLocalDateTime = LocalDateTime.now(ZoneId.of("Asia/Seoul"));

        /*7. 로그인 성공 시 DB Token 정보 갱신 */
        user.setAccessToken(tokenResponse.token());
        user.setRefreshToken(tokenResponse.refreshToken());
        user.setLoginErrorCount(0);
        user.setLastLoginDate(currentLocalDateTime);
        userRepository.save(user);

        /*8. 쿠키에 Access Token 추가*/
        tokenProvider.renewalAccessTokenInCookie(httpServletResponse, tokenResponse.token());

        /*로그인한 IP 정보 추가*/
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String ip = getClientIpAddress(request);


        /* 로그인 이력 추가 */
        l_login_key login_key = new l_login_key(currentLocalDateTime, parameter.id());
        l_login login = new l_login();
        login.setKey(login_key);
        login.setLoginIp(ip);
        loginHistoryRepository.save(login);

        return ResponseEntity.ok()
                .body(ItemResponse.<TokenResponse>builder()
                        .status(messageConfig.getCode("SUCCESS.CODE"))
                        .message(messageConfig.getMsg("LOGIN.SUCCESS.MSG"))
                        .item(tokenResponse)
                        .build());
    }

    @Transactional
    public ResponseEntity<LogoutResponse> logout(HttpServletRequest request, HttpServletResponse response) {
        String accessToken = tokenProvider.getTokenFromCookie(request);

        if (StringUtils.hasText(accessToken)) {
            try {
                String userId = tokenProvider.getUid(accessToken);
                tokenProvider.expirationToken(response);
                Optional<m_user> optionalEntity = userRepository.findById(userId);
                if (optionalEntity.isPresent()) {
                    optionalEntity.get().setAccessToken(null);
                    optionalEntity.get().setRefreshToken(null);
                }
            } catch (JwtException e) {
                throw new UnauthorizedException(CommonErrorCode.EXPIRED_TOKEN, e);
            } catch (PersistenceException e) {
                throw new ServiceException(CommonErrorCode.SERVICE_ERROR, e);
            }
        } else {
            throw new UnauthorizedException(CommonErrorCode.NOT_AUTHENTICATION, null);
        }

        return ResponseEntity.ok()
                .body(LogoutResponse.builder()
                        .status(messageConfig.getCode("SUCCESS.CODE"))
                        .message(messageConfig.getMsg("LOGOUT.SUCCESS.MSG"))
                        .build());
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");

        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }

        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }

        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("HTTP_X_FORWARDED_FOR");
        }

        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("HTTP_X_FORWARDED");
        }

        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("HTTP_X_CLUSTER_CLIENT_IP");
        }

        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("HTTP_CLIENT_IP");
        }

        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("REMOTE_ADDR");
        }

        try {
            ipAddress = ipAddress.replaceAll("0:0:0:0:0:0:0:1", InetAddress.getLocalHost().getHostAddress())
                                 .replaceAll("::1", InetAddress.getLocalHost().getHostAddress());
        } catch (UnknownHostException e) {
            ipAddress = ipAddress.replaceAll("0:0:0:0:0:0:0:1", "127.0.0.1")
                                 .replaceAll("::1", "127.0.0.1");
        }

        return ipAddress;
    }

}
