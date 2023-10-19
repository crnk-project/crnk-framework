package io.crnk.data.jpa.query.criteria;

import io.crnk.data.jpa.query.JpaQueryFactory;
import io.crnk.data.jpa.query.SingleTableInheritanceQueryTestBase;

import jakarta.persistence.EntityManager;

public class SingleTableInheritanceCriteriaTest extends SingleTableInheritanceQueryTestBase {

	@Override
	protected JpaQueryFactory createQueryFactory(EntityManager em) {
		return JpaCriteriaQueryFactory.newInstance();
	}

}
