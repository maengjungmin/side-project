package org.side.mjm.entity;

import jakarta.persistence.*;
import org.side.mjm.common.jpa.querydsl.annotation.DefaultSort;
import org.side.mjm.common.jpa.querydsl.annotation.SearchableField;
import org.side.mjm.common.jpa.querydsl.enumeration.SortDirection;
import org.side.mjm.entity.key.l_login_key;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "l_login")
@DefaultSort(columnName = { "loginDate", "userId" }, direction = { SortDirection.DESC, SortDirection.DESC })
public class l_login {

    @EmbeddedId
    @SearchableField(columnPath = {"key.loginDate", "key.userId"})
    private l_login_key key;

    @SearchableField
    @Column(name = "ip")
    private String loginIp;
}
