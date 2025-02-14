package org.side.mjm.config.jwt.record;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(description = "JWT Token 응답 DTO")
@Builder
public record TokenResponse(
        @Schema(description = "Access Token", example = "eyJhbGciOiJSUzI1NiJ9...", hidden = true)
        @JsonIgnore
        String token,
        @Schema(description = "Refresh Token", example = "eyJhbGciOiJSUzI1NiJ9...")
        String refreshToken,
        @Schema(description = "Token 타입", example = "Bearer")
        String tokenType,
        @Schema(description = "Token 만료 시간 (초)", example = "600000")
        Long expirationSeconds,
        @Schema(description = "비밀번호 갱신 여부 (갱신주기가 지난 경우 true 아니면 false)")
        @JsonInclude(JsonInclude.Include.NON_NULL)
        boolean isChangePassword) {
}