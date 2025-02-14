package org.side.mjm.domain.user.record;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import org.side.mjm.common.validation.annotation.ByteSize;

@Schema(description = "내 정보 조회")
public record UserSearchResponse(
        @Schema(description = "회원 ID / 이메일 형식", example = "aod0408@naver.com")
        String userId,
        @Schema(description = "회원 명", example = "맹정민")
        String userName,
        @Schema(description = "회원 휴대전화 / VARCHAR2(30)", example = "010-1234-5678")
        String mobilePhoneNumber,
        @Schema(description = "주소", example = "서울특별시 어딘가")
        String address,
        @Schema(description = "마지막 비밀번호 변경 일시", example = "2024-01-01")
        String passwordUpdateDate
) {
}
