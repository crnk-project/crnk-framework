package io.crnk.jpa.query.criteria;

import io.crnk.jpa.internal.query.backend.criteria.JpaCriteriaQueryExecutorImpl;
import io.crnk.jpa.model.TestEntity;
import io.crnk.jpa.query.BasicQueryTestBase;
import io.crnk.jpa.query.JpaQuery;
import io.crnk.jpa.query.JpaQueryFactory;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.Map;

public class BasicCriteriaTest extends BasicQueryTestBase {

	@Override
	protected JpaQueryFactory createQueryFactory(EntityManager em) {
		return JpaCriteriaQueryFactory.newInstance();
	}

	@Test
	@Ignore
	public void testEqualsInCollectionFilter() {
		// TODO invalid SQL generated, maybe due to use of list?
	}

	@Test
	public void testSetCached() {
		JpaQuery<TestEntity> builder = queryFactory.query(TestEntity.class);
		JpaCriteriaQueryExecutorImpl<TestEntity> executor = (JpaCriteriaQueryExecutorImpl<TestEntity>) builder.buildExecutor();
		executor.setCached(true);
		TypedQuery<TestEntity> typedQuery = executor.getTypedQuery();
		Map<String, Object> hints = typedQuery.getHints();
		Assert.assertTrue(hints.containsKey("org.hibernate.cacheable"));
	}
}
