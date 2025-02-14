package org.side.mjm.common.validation.regExp.enumeration;

import lombok.Getter;

@Getter
public enum RegExp {
    ONLY_NUMBER("^[0-9]+$"),
    ONLY_ALPHABET("^[a-zA-Z]+$"),
    ONLY_KOREAN("^[가-힣]+$"),

    ONLY_UPPER("^[A-Z]+$"),
    ONLY_LOWER("^[a-z]+$"),

    ONLY_CAR_NUMBER("^[가-힣0-9]*[가-힣]+[0-9]{4}$");

    private final String format;

    RegExp(String format) {
        this.format = format;
    }
}
