package io.crnk.data.jpa;

import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.data.jpa.model.TestEntity;
import io.crnk.data.jpa.query.criteria.JpaCriteriaQueryFactory;
import io.crnk.data.jpa.query.criteria.JpaCriteriaRepositoryFilterBase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaQuery;

public class JpaFilterTest extends AbstractJpaJerseyTest {

	private ResourceRepository<TestEntity, Long> testRepo;

	private JpaCriteriaRepositoryFilterBase filter;

	@Override
	@Before
	public void setup() {
		super.setup();
		testRepo = client.getRepositoryForType(TestEntity.class);
	}

	@Override
	protected void setupModule(JpaModuleConfig config, boolean server, EntityManager em) {
		if (server) {
			config.setQueryFactory(JpaCriteriaQueryFactory.newInstance());
			filter = Mockito.spy(new JpaCriteriaRepositoryFilterBase());
			config.addFilter(filter);
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
