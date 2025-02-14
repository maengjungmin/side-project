package org.side.mjm.common.validation.annotation;


import org.side.mjm.common.validation.enumeration.RegularExpression;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface FieldValid {
    String fieldName();

    RegularExpression pattern() default RegularExpression.ALL_PASS;

    int length() default Integer.MAX_VALUE;

    String message() default "";
}
