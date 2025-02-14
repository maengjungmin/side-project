package org.side.mjm.common.request;

import io.swagger.v3.oas.annotations.media.Schema;
import org.side.mjm.common.jpa.querydsl.enumeration.Operator;
import lombok.Builder;

@Schema(description = "동적 필터")
@Builder
public record DynamicFilter(
        @Schema(description = "조회 할 변수명", example = "userName")
        String field,
        @Schema(description = "조회 할 동작 [eq, contains, between, in, lte, gte]", example = "eq")
        Operator operator,
        @Schema(description = "조회 할 값", example = "이건")
        String value) {
}