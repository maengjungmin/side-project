package org.side.mjm.common.response.structure;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(description = "동적 헤더 목록 응답 구조체")
@Builder
public record DynamicHeaderResponse(
        @Schema(description = "filed", example = "round1")
        String filed,
        @Schema(description = "text", example = "1회차")
        String text) {
}
