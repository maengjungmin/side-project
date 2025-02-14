package org.side.mjm.common.validation.annotation;


import jakarta.validation.Constraint;
import org.side.mjm.common.validation.validator.ByteSizeValidator;

import java.lang.annotation.*;


@Target(ElementType.FIELD)  // 필드를 적용 대상으로 설정.
@Retention(RetentionPolicy.RUNTIME)  // 런타임 시간동안 유효.
@Constraint(validatedBy = ByteSizeValidator.class)  // 검증 클래스 지정.
@Documented
public @interface ByteSize {
    int min() default 0;  // 최소 바이트 크기.

    int max() default Integer.MAX_VALUE;  // 최대 바이트 크기.

    String message() default "Invalid byte size.";

    Class[] groups() default {};

    Class[] payload() default {};
}