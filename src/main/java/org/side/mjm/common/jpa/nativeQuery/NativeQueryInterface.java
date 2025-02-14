package org.side.mjm.common.jpa.nativeQuery;

/**
 * NativeQuery 동적 변경을 위한 함수인터페이스.
 *
 * @author MJM
 * @since 2024-07-11<br />
 */
@FunctionalInterface
public interface NativeQueryInterface {
    String query();
}
