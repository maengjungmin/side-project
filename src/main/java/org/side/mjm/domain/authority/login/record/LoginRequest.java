package org.side.mjm.domain.authority.login.record;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "로그인 요청")
public record LoginRequest(
        @Schema(description = "login Id", example = "geonlee")
        @NotNull
        String id,
        @Schema(description = "Login Password", example = "c1eN5eUROLqgste4R2FjrV3vL6+/WBW/wkgJPHpQmPYy7+YSBYF7PXkLOSbyMAUUMKy3XUeErvD/KUXFhQHBLgFNKMqSazQUFj3IDoXkhEkB8AmR8JEAz5H+d3Q4FRoEEf9vfsDRDbnn+FGw+d0QhNyQ+hYxhDEIGMtaH+Tcd8aLO36AZoHtsyo520znfwBEdMx2GaneNPQXnFU7yypd1m97E3XOVlbyKht1hodz3BL0ufDkQPiNKFWEw5yVTvfIwP2zPC1Jd3CkAt4JBOFVwVOr3JBgVTZg9DhBBQhoW4XLa6OUT2eOKovabFdpV0Y7wby7/Z4RN7vAeU9gNwtX3Q==")
        @NotNull
        String password) {
}
