package io.crnk.jpa.query;

import java.util.List;
import javax.persistence.EntityManager;

import io.crnk.core.queryspec.Direction;
import io.crnk.core.queryspec.FilterOperator;
import io.crnk.core.queryspec.PathSpec;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.jpa.model.TestEntity;
import io.crnk.jpa.query.criteria.JpaCriteriaQuery;
import io.crnk.jpa.query.criteria.JpaCriteriaQueryFactory;
import org.hibernate.Hibernate;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class StandaloneQueryFactoryTest extends AbstractJpaTest {

	@Test
	public void test() {
		// tag::docs[]
		JpaCriteriaQueryFactory queryFactory = JpaCriteriaQueryFactory.newInstance(em);

		PathSpec idAttr = PathSpec.of(TestEntity.ATTR_id);
		QuerySpec querySpec = new QuerySpec(TestEntity.class);
		querySpec.addFilter(idAttr.filter(FilterOperator.GT, 0L));
		querySpec.addSort(idAttr.sort(Direction.DESC));
		querySpec.includeRelation(PathSpec.of("oneRelatedValue"));

		JpaCriteriaQuery<TestEntity> query = queryFactory.query(TestEntity.class);
		JpaQueryExecutor<TestEntity> executor = query.buildExecutor(querySpec);
		List<TestEntity> resultList = executor.getResultList();
		// end::docs[]

		Assert.assertEquals(4, resultList.size());
		Assert.assertEquals(4, resultList.get(0).getId().intValue());
		Assert.assertEquals(1, resultList.get(3).getId().intValue());
		Assert.assertTrue(Hibernate.isInitialized(resultList.get(3).getOneRelatedValue()));
		Assert.assertFalse(Hibernate.isInitialized(resultList.get(3).getManyRelatedValues()));
	}

	@Override
	protected JpaQueryFactory createQueryFactory(EntityManager em) {
		return JpaCriteriaQueryFactory.newInstance();
	}
}
