package org.side.mjm.domain.user.record;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import org.side.mjm.common.validation.annotation.ByteSize;

@Schema(description = "회원 가입 요청")
public record UserCreateRequest(
        @Schema(description = "회원 ID / 이메일 형식", example = "aod0408@naver.com")
        @NotEmpty
        String userId,
        @Schema(description = "암호화 된 회원 비밀번호 / VARCHAR2(256)", example = "{SHA-256}{eOpq1iTGFDBOQGU/mMU/K1LTbtNxJfZ4gB7N/5LfxSg=}223bab005125ea391a4aaf5836d3ec4b525b206ef50bd327f6c23a587d5ca46e")
        @NotEmpty
        String password,
        @Schema(description = "회원 명", example = "맹정민")
        @NotEmpty
        @ByteSize(max = 10)
        String userName,
        @Schema(description = "회원 휴대전화 / VARCHAR2(30)", example = "010-1234-5678")
        @ByteSize(max = 30)
        String mobilePhoneNumber,
        @Schema(description = "주소", example = "서울특별시 어딘가")
        @ByteSize(max = 30)
        String address
) {
}
