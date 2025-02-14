package org.side.mjm.common.request;

import io.swagger.v3.oas.annotations.media.Schema;
import org.side.mjm.common.jpa.querydsl.enumeration.SortDirection;

@Schema(description = "동적 정렬")
public record DynamicSorter(
        @Schema(description = "정렬할 변수명", example = "updateDate")
        String field,
        @Schema(description = "정렬할 방향", example = "desc")
        SortDirection direction) {
}
