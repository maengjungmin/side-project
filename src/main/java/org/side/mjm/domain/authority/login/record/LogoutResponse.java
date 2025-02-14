package org.side.mjm.domain.authority.login.record;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "로그아웃 응답")
public record LogoutResponse(
        @Schema(description = "응답 코드", example = "OK")
        String status,
        @Schema(description = "응답 메시지", example = "로그아웃 하였습니다.")
        String message) {
}
