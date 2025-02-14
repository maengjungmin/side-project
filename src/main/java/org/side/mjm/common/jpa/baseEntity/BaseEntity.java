package org.side.mjm.common.jpa.baseEntity;

import jakarta.persistence.*;
import org.side.mjm.common.jpa.querydsl.annotation.SearchableField;
import lombok.Getter;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
public class BaseEntity {
    /* 생성 일시 */
//    @Column(name = "CRT_DT")
//    @Temporal(TemporalType.TIMESTAMP)
//    @CreatedDate
//    @SearchableField
//    private LocalDateTime createDate;

    /* 수정 일시 */
    @Column(name = "UPD_DT")
    @Temporal(TemporalType.TIMESTAMP)
    @LastModifiedDate
    @SearchableField
    private LocalDateTime updateDate;
}
