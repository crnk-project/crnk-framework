package io.crnk.jpa;

import javax.persistence.criteria.CriteriaQuery;

import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepositoryV2;
import io.crnk.jpa.model.TestEntity;
import io.crnk.jpa.query.criteria.JpaCriteriaRepositoryFilterBase;
import io.crnk.jpa.query.criteria.JpaCriteriaQueryFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

public class JpaFilterTest extends AbstractJpaJerseyTest {

	private ResourceRepositoryV2<TestEntity, Long> testRepo;

	private JpaCriteriaRepositoryFilterBase filter;

	@Override
	@Before
	public void setup() {
		super.setup();
		testRepo = client.getRepositoryForType(TestEntity.class);
	}

	@Override
	protected void setupModule(JpaModule module, boolean server) {
		super.setupModule(module, server);
		if (server) {
			module.setQueryFactory(JpaCriteriaQueryFactory.newInstance());
			filter = Mockito.spy(new JpaCriteriaRepositoryFilterBase());
			module.addFilter(filter);
		}
	}

	@Test
	public void test() {
		testRepo.findAll(new QuerySpec(TestEntity.class));

		ArgumentCaptor<CriteriaQuery> criteriaQueryCaptor = ArgumentCaptor.forClass(CriteriaQuery.class);
		Mockito.verify(filter, Mockito.times(1))
				.filterCriteriaQuery(Mockito.any(), Mockito.any(QuerySpec.class), criteriaQueryCaptor.capture());

		CriteriaQuery value = criteriaQueryCaptor.getValue();
		Assert.assertNotNull(value);
	}

}
