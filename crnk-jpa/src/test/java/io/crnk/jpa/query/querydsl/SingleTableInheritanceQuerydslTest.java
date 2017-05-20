package io.crnk.jpa.query.querydsl;

import io.crnk.jpa.query.JpaQueryFactory;
import io.crnk.jpa.query.SingleTableInheritanceQueryTestBase;

import javax.persistence.EntityManager;

public class SingleTableInheritanceQuerydslTest extends SingleTableInheritanceQueryTestBase {

	@Override
	protected JpaQueryFactory createQueryFactory(EntityManager em) {
		return QuerydslQueryFactory.newInstance();
	}
}
