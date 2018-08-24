package io.crnk.jpa.repository.querydsl;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import io.crnk.core.queryspec.QuerySpec;
import io.crnk.jpa.JpaEntityRepository;
import io.crnk.jpa.JpaModule;
import io.crnk.jpa.JpaRepositoryConfig;
import io.crnk.jpa.model.TestEntity;
import io.crnk.jpa.query.AbstractJpaTest;
import io.crnk.jpa.query.JpaQueryFactory;
import io.crnk.jpa.query.querydsl.QuerydslQueryFactory;
import io.crnk.jpa.query.querydsl.QuerydslRepositoryFilterBase;
import io.crnk.jpa.query.querydsl.QuerydslTranslationContext;
import org.junit.Test;
import org.mockito.Mockito;

@Transactional
public class QuerydslRepositoryFilterTest extends AbstractJpaTest {

	private QuerydslRepositoryFilterBase filter;

	@SuppressWarnings("unchecked")
	@Test
	public void translationInterceptor() {
		JpaEntityRepository<TestEntity, Long> repo =
				new JpaEntityRepository<>(module, JpaRepositoryConfig.create(TestEntity.class));


		QuerySpec querySpec = new QuerySpec(TestEntity.class);
		repo.findAll(querySpec);

		Mockito.verify(filter, Mockito.times(1)).filterQueryTranslation(Mockito.eq(repo), Mockito.eq(querySpec),
				Mockito.any(QuerydslTranslationContext.class));
	}

	@Override
	protected void setupModule(JpaModule module2) {
		filter = Mockito.spy(new QuerydslRepositoryFilterBase());
		module.addFilter(filter);
	}

	@Override
	protected JpaQueryFactory createQueryFactory(EntityManager em) {
		return QuerydslQueryFactory.newInstance();
	}
}
