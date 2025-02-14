package org.side.mjm.config.jwt.record;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class JwtValidDto {
    private boolean valid;
    private String userId;
    private String accessToken;
}
