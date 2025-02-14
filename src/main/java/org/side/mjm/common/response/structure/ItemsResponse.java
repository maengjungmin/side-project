package org.side.mjm.common.response.structure;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Builder
@Schema(description = "공통 복수 객체 응답 구조체")
public record ItemsResponse<T>(
        @Schema(description = "상태 코드", example = "OK")
        String status,

        @Schema(description = "응답 메시지", example = "응답 메시지")
        String message,

        @Schema(description = "정상 응답 데이터 개수", example = "1")
        @JsonInclude(JsonInclude.Include.NON_NULL)
        Long totalSize,

        @Schema(description = "오류 응답 데이터 개수", example = "1")
        @JsonInclude(JsonInclude.Include.NON_NULL)
        Long errorSize,

        @Schema(description = "복수 응답 객체 / LIST")
        @JsonInclude(JsonInclude.Include.NON_NULL)
        List<T> items) {
}
