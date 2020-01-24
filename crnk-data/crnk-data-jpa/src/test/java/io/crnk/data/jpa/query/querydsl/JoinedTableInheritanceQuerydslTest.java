package io.crnk.data.jpa.query.querydsl;

import io.crnk.data.jpa.query.JoinedTableInheritanceQueryTestBase;
import io.crnk.data.jpa.query.JpaQueryFactory;

import javax.persistence.EntityManager;

public class JoinedTableInheritanceQuerydslTest extends JoinedTableInheritanceQueryTestBase {

	@Override
	protected JpaQueryFactory createQueryFactory(EntityManager em) {
		return QuerydslQueryFactory.newInstance();
	}

}
