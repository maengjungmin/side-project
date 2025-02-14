package org.side.mjm.config.jwt;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.side.mjm.common.exception.code.CommonErrorCode;
import org.side.mjm.common.response.ResponseWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import static org.side.mjm.config.jwt.TokenProvider.AUTHORIZATION_FAIL_TYPE;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) {
        CommonErrorCode errorCode = (CommonErrorCode) request.getSession().getAttribute(AUTHORIZATION_FAIL_TYPE);
        if (errorCode != CommonErrorCode.DUPLICATION_LOGIN) {
            errorCode = CommonErrorCode.NOT_AUTHENTICATION;
        }
        ResponseWriter.setResponseWriter(response, errorCode.getResultCode(), errorCode.getResultMsg());
    }
}