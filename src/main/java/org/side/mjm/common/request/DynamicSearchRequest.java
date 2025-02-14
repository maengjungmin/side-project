package org.side.mjm.common.request;

import io.swagger.v3.oas.annotations.media.Schema;
import org.side.mjm.common.exception.code.CommonErrorCode;
import org.side.mjm.common.exception.custom.ServiceException;
import org.side.mjm.common.jpa.querydsl.enumeration.Operator;
import lombok.Builder;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Optional;

@Schema(description = "동적 조회 요청")
@Builder
public record DynamicSearchRequest(
        @Schema(description = "가져올 개수 / default: 10", example = "10")
        Integer take,
        @Schema(description = "Skip 할 개수 / default: 0", example = "0")
        Integer skip,
        @Schema(description = "조회 조건 구조체")
        ArrayList<DynamicFilter> filter,
        @Schema(description = "정렬 조건 구조체")
        ArrayList<DynamicSorter> sorter,
        ArrayList<DynamicFilter> noOffsetFilter) {
    public DynamicSearchRequest {
        take = (take == null) ? 10 : take;
        skip = (skip == null) ? 0 : skip;
    }

    public String getFieldValue(String fieldName) {
        if (StringUtils.isEmpty(fieldName)) {
            throw new ServiceException(CommonErrorCode.INVALID_PARAMETER, "'fieldName' can't be empty.");
        }
        return this.filter.stream()
                .filter(filter -> fieldName.equals(filter.field()))
                .map(DynamicFilter::value)
                .findFirst()
                .orElseThrow(() -> new ServiceException(CommonErrorCode.INVALID_PARAMETER, fieldName + " value doesn't exist."));
    }

    public String getFieldValue(String fieldName, Operator operator) {
        if (StringUtils.isEmpty(fieldName)) {
            throw new ServiceException(CommonErrorCode.INVALID_PARAMETER, "'fieldName' can't be empty.");
        }
        return this.filter.stream()
                .filter(dynamicFilter -> fieldName.equals(dynamicFilter.field()) && dynamicFilter.operator() == operator)
                .map(DynamicFilter::value)
                .findFirst()
                .orElseThrow(() -> new ServiceException(CommonErrorCode.INVALID_PARAMETER, fieldName + " value doesn't exist."));
    }

    public Optional<String> getFieldValueAsOptional(String fieldName) {
        if (StringUtils.isEmpty(fieldName)) {
            throw new ServiceException(CommonErrorCode.INVALID_PARAMETER, "'fieldName' can't be empty.");
        }
        return this.filter.stream()
                .filter(filter -> fieldName.equals(filter.field()))
                .map(DynamicFilter::value)
                .findFirst();
    }
}