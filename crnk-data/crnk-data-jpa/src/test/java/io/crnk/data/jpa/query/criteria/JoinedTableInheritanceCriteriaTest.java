package io.crnk.data.jpa.query.criteria;

import io.crnk.data.jpa.query.JoinedTableInheritanceQueryTestBase;
import io.crnk.data.jpa.query.JpaQueryFactory;

import jakarta.persistence.EntityManager;

public class JoinedTableInheritanceCriteriaTest extends JoinedTableInheritanceQueryTestBase {

	@Override
	protected JpaQueryFactory createQueryFactory(EntityManager em) {
		return JpaCriteriaQueryFactory.newInstance();
	}

}
