package org.side.mjm.common.jpa;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.side.mjm.common.request.DynamicFilter;
import org.side.mjm.common.request.DynamicSearchRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;
import java.util.List;

@NoRepositoryBean
public interface JpaDynamicRepository<T, ID extends Serializable> extends JpaRepository<T, ID> {
    /**
     * DynamicFilter 를 사용한 count 조회
     */
    Long countDynamic(DynamicFilter dynamicFilter);

    /**
     * DynamicFilter list 를 사용한 복수조건 count 조회
     */
    Long countDynamic(List<DynamicFilter> dynamicFilterList);

    /**
     * DynamicFilter 를 사용한 List 조회
     */
    List<T> findDynamic(DynamicFilter dynamicFilter);

    /**
     * DynamicFilter list 를 사용한 복수조건 List 조회
     */
    List<T> findDynamic(List<DynamicFilter> dynamicFilter);

    /**
     * DynamicRequest 를 사용한 복수조건, 복수 정렬 List 조회, No paging
     */
    List<T> findDynamic(DynamicSearchRequest dynamicRequest);

    /**
     * DynamicRequest 를 활용한 List 조회
     */
    Page<T> findDynamicWithPageable(DynamicSearchRequest dynamicSearchRequest);

    JPAQueryFactory getQueryFactory();

}
