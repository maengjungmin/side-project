package org.side.mjm.common.jpa.querydsl.enumeration;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;

import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Getter
public enum Operator {
    EQUAL("eq"), NOT_EQUAL("neq"), LIKE("contains"), BETWEEN("between"), IN("in"), LTE("lte"), GTE("gte"),
    LT("lt"), GT("gt");

    private static final Map<String, Operator> OPERATOR_MAP = Stream.of(values())
            .collect(Collectors.toMap(Operator::type, e -> e));
    private final String type;

    Operator(String type) {
        this.type = type;
    }

    public static Optional<Operator> value(String operator) {
        return Optional.ofNullable(OPERATOR_MAP.get(operator));
    }

    @JsonCreator
    public static Operator fromText(String operatorText) {
        for (Operator operator : Operator.values()) {
            if (operator.type().equals(operatorText)) {
                return operator;
            }
        }
        return null;
    }

    public static String getOperatorString() {
        StringJoiner stringJoiner = new StringJoiner(",");
        for (Operator operators : Operator.values()) {
            stringJoiner.add(operators.type());
        }
        return stringJoiner.toString();
    }

    public String type() {
        return this.type;
    }
}