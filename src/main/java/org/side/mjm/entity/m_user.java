package org.side.mjm.entity;

import jakarta.persistence.*;
import org.side.mjm.common.jpa.baseEntity.BaseEntity;
import org.side.mjm.common.jpa.querydsl.annotation.DefaultSort;
import org.side.mjm.common.jpa.querydsl.annotation.SearchableField;
import org.side.mjm.common.jpa.querydsl.enumeration.SortDirection;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "m_user")
@DefaultSort(columnName = "userId", direction = SortDirection.DESC)
public class m_user {

    @Id
    @SearchableField
    @Column(name = "user_id")
    private String userId;

    @Column(name = "password")
    private String password;

    @Column(name = "name")
    @SearchableField
    private String userName;

    @Column(name = "phone")
    @SearchableField
    private String mobilePhoneNumber;

    @Column(name = "address")
    private String address;

    @Column(name = "access_token")
    private String accessToken;

    @Column(name = "refresh_token")
    private String refreshToken;

    @Column(name = "last_login")
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime lastLoginDate;

    /* 패스워드갱신 일자 */
    @Column(name = "password_update_date")
    @Temporal(TemporalType.DATE)
    private LocalDate passwordUpdateDate;

    /* 로그인 오류 수 */
    @Column(name = "login_fail")
    private Integer loginErrorCount;
}
