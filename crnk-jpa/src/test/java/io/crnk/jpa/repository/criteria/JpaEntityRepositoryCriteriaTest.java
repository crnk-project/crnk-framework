package io.crnk.jpa.repository.criteria;

import io.crnk.jpa.query.JpaQueryFactory;
import io.crnk.jpa.query.criteria.JpaCriteriaQueryFactory;
import io.crnk.jpa.repository.JpaEntityRepositoryTestBase;

import javax.persistence.EntityManager;

public class JpaEntityRepositoryCriteriaTest extends JpaEntityRepositoryTestBase {

	@Override
	protected JpaQueryFactory createQueryFactory(EntityManager em) {
		return JpaCriteriaQueryFactory.newInstance();
	}
}
