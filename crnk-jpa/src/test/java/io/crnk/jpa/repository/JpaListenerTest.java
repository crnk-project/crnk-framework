package io.crnk.jpa.repository;

import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.jpa.JpaEntityRepository;
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
public class JpaListenerTest extends AbstractJpaTest {

	private JpaEntityRepository<TestEntity, Long> repo;

	@Override
	@Before
	public void setup() {
		super.setup();
		repo = new JpaEntityRepository<>(module, JpaRepositoryConfig.create(TestEntity.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void test() throws InstantiationException, IllegalAccessException {

		JpaRepositoryFilterBase filter = Mockito.spy(new JpaRepositoryFilterBase());
		module.addFilter(filter);

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

	@Test
	public void testaAddRemove() throws InstantiationException, IllegalAccessException {
		JpaRepositoryFilterBase filter = Mockito.spy(new JpaRepositoryFilterBase());
		module.addFilter(filter);
		Assert.assertEquals(1, module.getFilters().size());
		module.removeFilter(filter);
		Assert.assertEquals(0, module.getFilters().size());
	}

	@Override
	protected JpaQueryFactory createQueryFactory(EntityManager em) {
		return QuerydslQueryFactory.newInstance();
	}

}
