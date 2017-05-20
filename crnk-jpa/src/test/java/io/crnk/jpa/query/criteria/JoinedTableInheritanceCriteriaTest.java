package io.crnk.jpa.query.criteria;

import io.crnk.jpa.query.JoinedTableInheritanceQueryTestBase;
import io.crnk.jpa.query.JpaQueryFactory;

import javax.persistence.EntityManager;

public class JoinedTableInheritanceCriteriaTest extends JoinedTableInheritanceQueryTestBase {

	@Override
	protected JpaQueryFactory createQueryFactory(EntityManager em) {
		return JpaCriteriaQueryFactory.newInstance();
	}

}
