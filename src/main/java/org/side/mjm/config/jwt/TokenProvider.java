package org.side.mjm.config.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.DecodingException;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.side.mjm.common.variable.CommonVariables;
import org.side.mjm.config.jwt.record.TokenResponse;
import org.side.mjm.config.rsa.RsaProvider;
import org.side.mjm.entity.m_user;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TokenProvider implements InitializingBean {
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String JWT_COOKIE_NAME = "AUT";
    public static final String AUTH_KEY = "auth";
    public static final String AUTHORIZATION_FAIL_TYPE = "AUT_FT";
    private static final Logger LOGGER = LoggerFactory.getLogger(TokenProvider.class);
    private static final String TOKEN_TYPE = "Bearer";
    private static final String REFRESH_TOKEN = "refresh";
    /*Access token 만료 기한 : 24시간*/
    private static long tokenValidityInMilliseconds = 12 * (1000 * 3600);
    /*Refresh token 만료 기한 : 일주일*/
    private static long refreshTokenValidityInMilliseconds = 24 * (1000 * 3600);
    private final RsaProvider rsaProvider;

//    private final ParameterRepository parameterRepository;

    @Value("${jwt.auth-key}")
    private String authoritiesKey;
    private Key signingKey;

    @Override
    public void afterPropertiesSet() throws Exception {
        this.signingKey = rsaProvider.getPrivateKey();
    }

    @PostConstruct
    public void setLogoutHour() {
//        List<M_OP_PARAM> parameter = parameterRepository.findByKeyMainId("LOGOUT_EXPIRED_TIME");
        long logoutHour = (24 * (1000 * 3600));
//        if (ObjectUtils.isEmpty(parameter)) {
//            LOGGER.info("Parameter table 에 'LOGOUT_EXPIRED_TIME' 이 없어, 24 Hour 로 초기화 합니다.");
//        } else {
//            logoutHour = (Long.parseLong(parameter.get(0).getSettingValue()) * (1000 * 3600));
//        }
        refreshTokenValidityInMilliseconds = logoutHour;
        tokenValidityInMilliseconds = refreshTokenValidityInMilliseconds / 2;
    }

    public String createAccessToken(Authentication authentication) {
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
        long now = (new Date()).getTime();
        Date validity = new Date(now + tokenValidityInMilliseconds);

        return Jwts.builder()
                .setSubject(authentication.getName())
                .claim(AUTH_KEY, authorities)
                .signWith(this.signingKey, SignatureAlgorithm.RS256)
                .setExpiration(validity)
                .compact();
    }

    public TokenResponse createToken(Authentication authentication, Boolean isChangePassword, m_user entity) {
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
        long now = (new Date()).getTime();
        Date validity = new Date(now + tokenValidityInMilliseconds);
        Date refreshValidity = new Date(now + refreshTokenValidityInMilliseconds);

        return TokenResponse.builder()
                .token(
                        Jwts.builder()
                                .setSubject(authentication.getName())
                                .claim(AUTH_KEY, authorities)
                                .claim("name", entity.getUserName())
                                .signWith(this.signingKey, SignatureAlgorithm.RS256)
                                .setExpiration(validity)
                                .compact())
                .refreshToken(
                        Jwts.builder()
                                .setSubject(authentication.getName())
                                .claim(AUTH_KEY, authorities)
                                .claim("name", entity.getUserName())
                                .signWith(this.signingKey, SignatureAlgorithm.RS256)
                                .setExpiration(refreshValidity)
                                .compact())
                .tokenType(TOKEN_TYPE)
                .isChangePassword(isChangePassword)
                .expirationSeconds(tokenValidityInMilliseconds)
                .build();
    }

    public Authentication getAuthentication(String token) {
        Claims claims = getClaims(token);
        Collection<? extends GrantedAuthority> authorities = new ArrayList<>();
//                .stream(claims.get(AUTH_KEY).toString().split(","))
//                .map(SimpleGrantedAuthority::new)
//                .collect(Collectors.toList());
        User principal = new User(claims.getSubject(), "", authorities);
        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    public Claims getClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(this.signingKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) { // Access Token
            return e.getClaims();
        }
    }

    public void expirationToken(HttpServletResponse httpServletResponse) {
        Cookie cookie = new Cookie(JWT_COOKIE_NAME, null);
        cookie.setMaxAge(0);
//        cookie.setPath(CommonVariables.getPropertyValue("server.servlet.context-path"));
        cookie.setPath("/");
        httpServletResponse.addCookie(cookie);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(this.signingKey).build().parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            LOGGER.info("Invalid jwt signature.");
        } catch (ExpiredJwtException e) {
            LOGGER.error("Access token is expired.");
        } catch (UnsupportedJwtException e) {
            LOGGER.info("This jwt token is not supported.");
        } catch (IllegalArgumentException e) {
            LOGGER.info("Invalid jwt token.");
        } catch (DecodingException e) {
            LOGGER.info("JWT token decoding failed");
        }
        return false;
    }

    public String getUid(String token) throws MalformedJwtException, ExpiredJwtException {
        return Jwts.parserBuilder().setSigningKey(this.signingKey).build().parseClaimsJws(token).getBody().getSubject();
    }

    public String getTokenFromRequest(HttpServletRequest request) throws NullPointerException {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        String token = bearerToken.substring(7);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(TOKEN_TYPE + " ") && !"null".equals(token)) {
            return bearerToken.substring(7);
        }
        return null;
    }

    public String getTokenFromCookie(HttpServletRequest httpServletRequest) {
        Cookie[] cookies = httpServletRequest.getCookies();
        String requestURI = httpServletRequest.getRequestURI().replace(CommonVariables.CONTEXT_PATH, "");
        if (cookies != null) {
            Optional<String> optionalAccessToken = Arrays.stream(cookies)
                    .filter(cookie -> JWT_COOKIE_NAME.equals(cookie.getName()))
                    .map(Cookie::getValue)
                    .findFirst();
            if (optionalAccessToken.isPresent()) {
                return doXssFilter(optionalAccessToken.get());
            }
        }
        LOGGER.error("Access token in cookie does not exist. request URI: {}", requestURI);
        return null;
    }

    public void renewalAccessTokenInCookie(HttpServletResponse httpServletResponse, String newAccessToken) {
        Cookie cookie = new Cookie(JWT_COOKIE_NAME, newAccessToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        httpServletResponse.addCookie(cookie);
    }

    private String doXssFilter(String origin) {
        return origin.replaceAll("'", "&#x27;").replaceAll("\"", "&quot;").replaceAll("\\(", "&#40;")
                .replaceAll("\\)", "&#41;").replaceAll("/", "&#x2F;").replaceAll("<", "&lt;")
                .replaceAll(">", "&gt;").replaceAll("&", "&amp;");
    }

}