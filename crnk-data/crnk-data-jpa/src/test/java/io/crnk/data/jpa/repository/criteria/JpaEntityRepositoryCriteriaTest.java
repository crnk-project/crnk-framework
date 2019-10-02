package io.crnk.data.jpa.repository.criteria;

import io.crnk.data.jpa.query.JpaQueryFactory;
import io.crnk.data.jpa.query.criteria.JpaCriteriaQueryFactory;
import io.crnk.data.jpa.JpaEntityRepositoryTestBase;

import javax.persistence.EntityManager;

public class JpaEntityRepositoryCriteriaTest extends JpaEntityRepositoryTestBase {

	@Override
	protected JpaQueryFactory createQueryFactory(EntityManager em) {
		return JpaCriteriaQueryFactory.newInstance();
	}
}
