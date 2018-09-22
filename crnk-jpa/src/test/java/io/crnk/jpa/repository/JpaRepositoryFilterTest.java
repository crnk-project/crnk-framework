package io.crnk.jpa.repository;

import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.jpa.JpaEntityRepository;
import io.crnk.jpa.JpaModule;
import io.crnk.jpa.JpaRepositoryConfig;
import io.crnk.jpa.JpaRepositoryFilterBase;
import io.crnk.jpa.model.TestEntity;
import io.crnk.jpa.query.AbstractJpaTest;
import io.crnk.jpa.query.JpaQuery;
import io.crnk.jpa.query.JpaQueryExecutor;
import io.crnk.jpa.query.JpaQueryFactory;
import io.crnk.jpa.query.querydsl.QuerydslQueryFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

@Transactional
public class JpaRepositoryFilterTest extends AbstractJpaTest {

	private JpaEntityRepository<TestEntity, Long> repo;

	private JpaRepositoryFilterBase filter;

	@Override
	@Before
	public void setup() {
		super.setup();
		repo = new JpaEntityRepository<>(module, JpaRepositoryConfig.create(TestEntity.class));
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
	protected void setupModule(JpaModule module) {
		filter = Mockito.spy(new JpaRepositoryFilterBase());
		module.addFilter(filter);
	}

	@Override
	protected JpaQueryFactory createQueryFactory(EntityManager em) {
		return QuerydslQueryFactory.newInstance();
	}

}
