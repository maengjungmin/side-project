package org.side.mjm.common.jpa.querydsl.enumeration;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum SortDirection {
    ASC("asc"), DESC("desc");

    private static final Map<String, SortDirection> ORDER_MAP = Stream.of(values())
            .collect(Collectors.toMap(SortDirection::direction, e -> e));
    private final String direction;

    SortDirection(String direction) {
        this.direction = direction;
    }

    public static Optional<SortDirection> valueOfOrder(String direction) {
        return Optional.ofNullable(ORDER_MAP.get(direction));
    }

    /**
     * @return SortDirections type String
     */
    public static String getSorDirectionString() {
        StringJoiner stringJoiner = new StringJoiner(",");
        for (SortDirection sortDirection : SortDirection.values()) {
            stringJoiner.add(sortDirection.direction());
        }
        return stringJoiner.toString();
    }

    /**
     * RequestBody 에서 String to Enum 시 활용
     */
    @JsonCreator
    public static SortDirection fromText(String sortDirectionsText) {
        for (SortDirection sortDirections : SortDirection.values()) {
            if (sortDirections.direction().equalsIgnoreCase(sortDirectionsText)) {
                return sortDirections;
            }
        }
        return null;
    }

    public String direction() {
        return this.direction;
    }
}
