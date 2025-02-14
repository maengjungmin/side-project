package org.side.mjm.entity.key;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Embeddable
public class l_login_key implements Serializable {

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "login_date")
    private LocalDateTime loginDate;

    @Column(name = "user_id")
    private String userId;
}
