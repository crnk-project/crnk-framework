package io.crnk.jpa.query.querydsl;

import io.crnk.jpa.query.JoinedTableInheritanceQueryTestBase;
import io.crnk.jpa.query.JpaQueryFactory;

import javax.persistence.EntityManager;

public class JoinedTableInheritanceQuerydslTest extends JoinedTableInheritanceQueryTestBase {

	@Override
	protected JpaQueryFactory createQueryFactory(EntityManager em) {
		return QuerydslQueryFactory.newInstance();
	}

}
