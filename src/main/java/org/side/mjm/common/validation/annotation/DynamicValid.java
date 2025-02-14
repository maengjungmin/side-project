package org.side.mjm.common.validation.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.side.mjm.common.validation.validator.DynamicValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = DynamicValidator.class)
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface DynamicValid {
    String[] essentialFields() default "";
    FieldValid[] fieldValidations() default {};

    String message() default "";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
