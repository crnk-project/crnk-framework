package io.crnk.data.jpa.repository;

import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.data.jpa.JpaEntityRepository;
import io.crnk.data.jpa.JpaModuleConfig;
import io.crnk.data.jpa.JpaRepositoryConfig;
import io.crnk.data.jpa.JpaRepositoryFilterBase;
import io.crnk.data.jpa.internal.JpaRepositoryUtils;
import io.crnk.data.jpa.model.TestEntity;
import io.crnk.data.jpa.query.AbstractJpaTest;
import io.crnk.data.jpa.query.JpaQuery;
import io.crnk.data.jpa.query.JpaQueryExecutor;
import io.crnk.data.jpa.query.JpaQueryFactory;
import io.crnk.data.jpa.query.querydsl.QuerydslQueryFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;

@Transactional
public class JpaRepositoryFilterTest extends AbstractJpaTest {

    private JpaEntityRepository<TestEntity, Long> repo;

    private JpaRepositoryFilterBase filter;

    @Override
    @Before
    public void setup() {
        super.setup();

        JpaRepositoryConfig<TestEntity> config = JpaRepositoryConfig.create(TestEntity.class);
        JpaRepositoryUtils.setDefaultConfig(module.getConfig(), config);
        repo = new JpaEntityRepository<>(config);
        repo.setResourceRegistry(resourceRegistry);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void test() {
        QuerySpec querySpec = new QuerySpec(TestEntity.class);
        ResourceList<TestEntity> list = repo.findAll(querySpec);
        Assert.assertEquals(5, list.size());

        Mockito.verify(filter, Mockito.times(1)).filterQuerySpec(Mockito.eq(repo), Mockito.eq(querySpec));
        Mockito.verify(filter, Mockito.times(1)).filterResults(Mockito.eq(repo), Mockito.eq(querySpec), Mockito.eq(list));
        Mockito.verify(filter, Mockito.times(1)).filterExecutor(Mockito.eq(repo), Mockito.eq(querySpec),
                Mockito.any(JpaQueryExecutor.class));
        Mockito.verify(filter, Mockito.times(1)).filterTuples(Mockito.eq(repo), Mockito.eq(querySpec), Mockito.anyList());
        Mockito.verify(filter, Mockito.times(1)).filterQuery(Mockito.eq(repo), Mockito.eq(querySpec),
                Mockito.any(JpaQuery.class));
    }

    @Override
    protected void setupModule(JpaModuleConfig module) {
        filter = Mockito.spy(new JpaRepositoryFilterBase());
        module.addFilter(filter);
    }

    @Override
    protected JpaQueryFactory createQueryFactory(EntityManager em) {
        return QuerydslQueryFactory.newInstance();
    }

}
