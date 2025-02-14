package org.side.mjm.common.response.structure;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "공통 에러 응답 구조체")
public record ErrorResponse(
        @Schema(description = "응답 코드", example = "ERR_SVR_01")
        String status,
        @Schema(description = "응답 메시지", example = "요청하신 서비스에 문제가 있습니다.")
        String message,
        @Schema(description = "상세 메시지", example = "관리자에게 문의하세요!")
        @JsonInclude(JsonInclude.Include.NON_NULL)
        String detailMessage) {
}
