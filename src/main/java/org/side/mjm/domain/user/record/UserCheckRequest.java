package org.side.mjm.domain.user.record;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
@Schema(description = "회원 중복 확인 요청")
public record UserCheckRequest(
        @Schema(description = "회원 ID / 이메일 형식", example = "aod0408@naver.com")
        @NotEmpty
        String userId){}