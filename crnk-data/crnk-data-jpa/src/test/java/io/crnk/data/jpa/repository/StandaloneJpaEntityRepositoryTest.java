package io.crnk.data.jpa.repository;

import io.crnk.core.queryspec.FilterOperator;
import io.crnk.core.queryspec.FilterSpec;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.data.jpa.JpaEntityRepository;
import io.crnk.data.jpa.JpaRepositoryConfig;
import io.crnk.data.jpa.model.TestEntity;
import io.crnk.data.jpa.query.AbstractJpaTest;
import io.crnk.data.jpa.query.JpaQueryFactory;
import io.crnk.data.jpa.query.criteria.JpaCriteriaQueryFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import java.util.Arrays;
import java.util.List;

@Transactional
public class StandaloneJpaEntityRepositoryTest extends AbstractJpaTest {

	protected JpaEntityRepository<TestEntity, Long> standaloneRepo;

	@Override
	@Before
	public void setup() {
		super.setup();

		// does not make use of JpaModule!
		JpaRepositoryConfig<TestEntity> repositoryConfig = JpaRepositoryConfig.create(TestEntity.class);
		repositoryConfig.setQueryFactory(JpaCriteriaQueryFactory.newInstance(em));
		standaloneRepo = new JpaEntityRepository<>(repositoryConfig);
		standaloneRepo.setResourceRegistry(resourceRegistry);
	}

	@Override
	protected JpaQueryFactory createQueryFactory(EntityManager em) {
		return JpaCriteriaQueryFactory.newInstance();
	}

	@Test
	public void test() {
		QuerySpec querySpec = new QuerySpec(TestEntity.class);
		querySpec.addFilter(new FilterSpec(Arrays.asList("longValue"), FilterOperator.EQ, 2L));
		List<TestEntity> list = standaloneRepo.findAll(querySpec);

		Assert.assertEquals(1, list.size());
		TestEntity entity = list.get(0);
		Assert.assertEquals(2, entity.getId().longValue());
		Assert.assertEquals(2L, entity.getLongValue());
	}

}
