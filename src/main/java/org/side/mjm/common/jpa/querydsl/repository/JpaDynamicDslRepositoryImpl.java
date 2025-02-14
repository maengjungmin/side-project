package org.side.mjm.common.jpa.querydsl.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.core.types.dsl.PathBuilderFactory;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NamedEntityGraph;
import org.side.mjm.common.contextHolder.ApplicationContextHolder;
import org.side.mjm.common.jpa.JpaDynamicRepository;
import org.side.mjm.common.jpa.querydsl.DynamicBooleanBuilder;
import org.side.mjm.common.request.DynamicFilter;
import org.side.mjm.common.request.DynamicSearchRequest;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class JpaDynamicDslRepositoryImpl<T, ID extends Serializable> extends SimpleJpaRepository<T, ID> implements JpaDynamicRepository<T, ID> {

    private final DynamicBooleanBuilder dynamicBooleanBuilder;
    private final JPAQueryFactory queryFactory;
    private final Class<T> entity;
    private final PathBuilder<T> pathBuilder;
    private final EntityManager entityManager;
    private final String HINT_NAME = "jakarta.persistence.loadgraph";

    public JpaDynamicDslRepositoryImpl(JpaEntityInformation<T, ?> entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);
        this.queryFactory = new JPAQueryFactory(entityManager);
        this.entity = entityInformation.getJavaType();
        this.pathBuilder = new PathBuilderFactory().create(this.entity);
        this.dynamicBooleanBuilder = ApplicationContextHolder.getContext().getBean(DynamicBooleanBuilder.class);
        this.entityManager = entityManager;
    }

    public JPAQueryFactory getQueryFactory() {
        return this.queryFactory;
    }

    @Override
    public Long countDynamic(DynamicFilter dynamicFilter) {
        List<DynamicFilter> dynamicFilters = new ArrayList<>() {{
            add(dynamicFilter);
        }};
        BooleanBuilder booleanBuilder = dynamicBooleanBuilder.generateConditions(this.entity, dynamicFilters);
        return this.queryFactory
                .select(this.pathBuilder.count())
                .from(this.pathBuilder)
                .where(booleanBuilder)
                .fetchOne();
    }

    @Override
    public Long countDynamic(List<DynamicFilter> dynamicFilters) {
        BooleanBuilder booleanBuilder = dynamicBooleanBuilder.generateConditions(this.entity, dynamicFilters);
        return this.queryFactory
                .select(this.pathBuilder.count())
                .from(this.pathBuilder)
                .where(booleanBuilder)
                .fetchOne();
    }

    @Override
    public List<T> findDynamic(DynamicFilter dynamicFilter) {
        List<DynamicFilter> dynamicFilters = new ArrayList<>() {{
            add(dynamicFilter);
        }};
        BooleanBuilder booleanBuilder = dynamicBooleanBuilder.generateConditions(this.entity, dynamicFilters);
        JPAQuery<T> query = this.queryFactory
                .selectFrom(this.pathBuilder)
                .where(booleanBuilder);
        checkNamedEntityGraph(query);
        return query.fetch();
    }

    @Override
    public List<T> findDynamic(List<DynamicFilter> dynamicFilters) {
        BooleanBuilder booleanBuilder = dynamicBooleanBuilder.generateConditions(this.entity, dynamicFilters);
        JPAQuery<T> query = this.queryFactory
                .selectFrom(this.pathBuilder)
                .where(booleanBuilder);
        checkNamedEntityGraph(query);
        return query.fetch();
    }

    @Override
    public List<T> findDynamic(DynamicSearchRequest dynamicRequest) {
        BooleanBuilder booleanBuilder = dynamicBooleanBuilder.generateConditions(this.entity, dynamicRequest.filter());
        List<OrderSpecifier<?>> orderSpecifiers = dynamicBooleanBuilder.generateSort(this.entity, dynamicRequest.sorter());
        JPAQuery<T> query = this.queryFactory
                .selectFrom(this.pathBuilder)
                .where(booleanBuilder)
                .orderBy(orderSpecifiers.toArray(OrderSpecifier[]::new));
        checkNamedEntityGraph(query);
        return query.fetch();
    }

    @Override
    public Page<T> findDynamicWithPageable(DynamicSearchRequest dynamicSearchRequest) {
        BooleanBuilder booleanBuilder = dynamicBooleanBuilder.generateConditions(this.entity, dynamicSearchRequest.filter());
        List<OrderSpecifier<?>> orderSpecifiers = dynamicBooleanBuilder.generateSort(this.entity, dynamicSearchRequest.sorter());
        Long totalSize = countDynamic(dynamicSearchRequest.filter());
        totalSize = (totalSize == null) ? 0L : totalSize;
        Pageable pageable = PageRequest.of(dynamicSearchRequest.skip() / dynamicSearchRequest.take(), dynamicSearchRequest.take());
        JPAQuery<T> query = this.queryFactory
                .selectFrom(this.pathBuilder)
                .where(booleanBuilder)
                .orderBy(orderSpecifiers.toArray(OrderSpecifier[]::new))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());
        checkNamedEntityGraph(query);
        List<T> list = query.fetch();
        return new PageImpl<>(list, pageable, totalSize);
    }

    private String getNamedEntityGraph() {
        NamedEntityGraph namedEntityGraph = this.entity.getAnnotation(NamedEntityGraph.class);
        if (ObjectUtils.isEmpty(namedEntityGraph)) {
            return null;
        }
        return namedEntityGraph.name();
    }

    private void checkNamedEntityGraph(JPAQuery<T> query) {
        String namedEntityGraphName = getNamedEntityGraph();
        if (StringUtils.isNotEmpty(namedEntityGraphName)) {
            query.setHint(this.HINT_NAME, this.entityManager.getEntityGraph(namedEntityGraphName));
        }
    }
}