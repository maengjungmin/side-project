package org.side.mjm.common.validation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.side.mjm.common.validation.annotation.ByteSize;
import lombok.SneakyThrows;

import java.nio.charset.StandardCharsets;


public class ByteSizeValidator implements ConstraintValidator<ByteSize, String> {
    private int min;
    private int max;

    @Override
    public void initialize(ByteSize annotation) {
        this.min = annotation.min();
        this.max = annotation.max();
    }

    @Override
    @SneakyThrows
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        if (value == null) {
            return true;
        }
        int byteSize = value.getBytes(StandardCharsets.UTF_8).length;

        return byteSize >= min && byteSize <= max;
    }
}