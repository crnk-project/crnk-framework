package io.crnk.jpa.query.criteria;

import io.crnk.jpa.query.JpaQueryFactory;
import io.crnk.jpa.query.SingleTableInheritanceQueryTestBase;

import javax.persistence.EntityManager;

public class SingleTableInheritanceCriteriaTest extends SingleTableInheritanceQueryTestBase {

	@Override
	protected JpaQueryFactory createQueryFactory(EntityManager em) {
		return JpaCriteriaQueryFactory.newInstance();
	}

}
