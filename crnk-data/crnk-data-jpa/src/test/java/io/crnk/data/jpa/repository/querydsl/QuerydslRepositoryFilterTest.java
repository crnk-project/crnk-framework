package io.crnk.data.jpa.repository.querydsl;

import io.crnk.core.queryspec.QuerySpec;
import io.crnk.data.jpa.JpaEntityRepository;
import io.crnk.data.jpa.JpaModuleConfig;
import io.crnk.data.jpa.JpaRepositoryConfig;
import io.crnk.data.jpa.model.TestEntity;
import io.crnk.data.jpa.query.AbstractJpaTest;
import io.crnk.data.jpa.query.JpaQueryFactory;
import io.crnk.data.jpa.query.querydsl.QuerydslQueryFactory;
import io.crnk.data.jpa.query.querydsl.QuerydslRepositoryFilterBase;
import io.crnk.data.jpa.query.querydsl.QuerydslTranslationContext;
import org.junit.Test;
import org.mockito.Mockito;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@Transactional
public class QuerydslRepositoryFilterTest extends AbstractJpaTest {

    private QuerydslRepositoryFilterBase filter;

    @SuppressWarnings("unchecked")
    @Test
    public void translationInterceptor() {
        JpaRepositoryConfig<TestEntity> config = JpaRepositoryConfig.create(TestEntity.class);
        config.setQueryFactory(module.getConfig().getQueryFactory());
        JpaEntityRepository<TestEntity, Long> repo = new JpaEntityRepository<>(config);
        repo.setResourceRegistry(resourceRegistry);


        QuerySpec querySpec = new QuerySpec(TestEntity.class);
        repo.findAll(querySpec);

        Mockito.verify(filter, Mockito.times(1)).filterQueryTranslation(Mockito.eq(repo), Mockito.eq(querySpec),
                Mockito.any(QuerydslTranslationContext.class));
    }

    @Override
    protected void setupModule(JpaModuleConfig config) {
        filter = Mockito.spy(new QuerydslRepositoryFilterBase());
        config.addFilter(filter);
    }

    @Override
    protected JpaQueryFactory createQueryFactory(EntityManager em) {
        return QuerydslQueryFactory.newInstance();
    }
}
