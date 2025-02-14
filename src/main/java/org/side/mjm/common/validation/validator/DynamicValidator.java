package org.side.mjm.common.validation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.side.mjm.common.exception.code.CommonErrorCode;
import org.side.mjm.common.exception.custom.ServiceException;
import org.side.mjm.common.request.DynamicFilter;
import org.side.mjm.common.request.DynamicSearchRequest;
import org.side.mjm.common.validation.annotation.DynamicValid;
import org.side.mjm.common.validation.annotation.FieldValid;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DynamicValidator implements ConstraintValidator<DynamicValid, DynamicSearchRequest> {

    private List<String> essentialFields = new ArrayList<>();
    private List<FieldValid> fieldValidations;

    @Override
    public void initialize(DynamicValid constraintAnnotation) {
        if (constraintAnnotation.essentialFields().length > 0 && !constraintAnnotation.essentialFields()[0].isEmpty()) {
            this.essentialFields = List.of(constraintAnnotation.essentialFields());
        }
        this.fieldValidations = List.of(constraintAnnotation.fieldValidations());
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(DynamicSearchRequest dynamicRequest, ConstraintValidatorContext context) {
        if (ObjectUtils.isNotEmpty(this.essentialFields) && ObjectUtils.isEmpty(dynamicRequest.filter())) {
            throw new ServiceException(CommonErrorCode.INVALID_PARAMETER, "Filter cannot be empty.");
        }
        if (ObjectUtils.isEmpty(dynamicRequest.filter())) {
            return true;
        }
        //Filter only non-null values
        Map<String, String> filterFields = dynamicRequest.filter().stream()
                .filter(dynamicFilter -> ObjectUtils.isNotEmpty(dynamicFilter.value()))
                .collect(Collectors.toMap(DynamicFilter::field, DynamicFilter::value));
        //EssentialField check
        List<String> notPresentList = this.essentialFields.stream()
                .filter(essentialField -> !filterFields.containsKey(getFieldName(essentialField, 0)))
                .map(essentialField -> (essentialField.contains(":"))
                        ? essentialField.split(":")[1].trim() + " value is required value."
                        : essentialField)
                .toList();
        //Validation check
        List<String> inValidFieldList = this.fieldValidations.stream()
                .filter(fieldValid -> filterFields.containsKey(getFieldName(fieldValid.fieldName(), 0)))
                .filter(fieldValid -> {
                    int byteSize = filterFields.get(getFieldName(fieldValid.fieldName(), 0)).getBytes(StandardCharsets.UTF_8).length;
                    return !Pattern.compile(fieldValid.pattern().format())
                            .matcher(filterFields.get(getFieldName(fieldValid.fieldName(), 0))).matches()
                            || byteSize > fieldValid.length();
                })
                .map(fieldValid -> (StringUtils.isEmpty(fieldValid.message()))
                        ? getFieldName(fieldValid.fieldName(), 1) + " value is invalid."
                        : fieldValid.message())
                .toList();
        List<String> invalidList = Stream.concat(notPresentList.stream(), inValidFieldList.stream()).toList();
        if (invalidList.size() > 0) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(CommonErrorCode.INVALID_PARAMETER.getResultMsg())
                    .addPropertyNode(String.join(", ", invalidList))
                    .addConstraintViolation();
        }
        return invalidList.size() == 0;
    }

    private String getFieldName(String fieldName, int index) {
        return (fieldName.contains(":")) ? fieldName.split(":")[index].trim() : fieldName.trim();
    }
}
