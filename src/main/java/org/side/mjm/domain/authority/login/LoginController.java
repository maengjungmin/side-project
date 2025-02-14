package org.side.mjm.domain.authority.login;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.side.mjm.common.response.structure.ItemResponse;
import org.side.mjm.config.jwt.record.TokenResponse;
import org.side.mjm.domain.authority.login.record.LoginRequest;
import org.side.mjm.domain.authority.login.record.LogoutResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "[API-001] 회원 로그인/로그아웃")
@RequiredArgsConstructor
public class LoginController {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoginController.class);
    private final LoginService loginService;

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "회원 로그인 요청", description = """
            ※id/pw 를 전달 받아 인증에 성공한 경우 Cookie 에 Access Token 을 전송 한다.
            ※Password 는 RSA public key 로 암호화 한 데이터를 전송한다.

            Cookie 를 활용하기 때문에 로그인 성공 후 Operation 요청 가능.
            Access Token 만료 시에만 Refresh Token 을 Authorization 으로 전송.
            """,
            operationId = "API-001-01"
    )
    public ResponseEntity<ItemResponse<TokenResponse>> login(@Valid @RequestBody LoginRequest parameter
            , HttpServletResponse httpServletResponse) {
        return loginService.login(parameter, httpServletResponse);
    }

    @PostMapping(value = "/logout", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "회원 로그아웃 요청", description = """
             No parameters
            """,
            operationId = "API-001-02"
    )
    public ResponseEntity<LogoutResponse> logout(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        return loginService.logout(httpServletRequest, httpServletResponse);
    }
}
