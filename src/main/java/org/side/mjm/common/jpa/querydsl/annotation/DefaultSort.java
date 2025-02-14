package org.side.mjm.common.jpa.querydsl.annotation;

import org.side.mjm.common.jpa.querydsl.enumeration.SortDirection;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DefaultSort {
    String[] columnName();

    SortDirection[] direction();
}
