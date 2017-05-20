package io.crnk.jpa.query.querydsl;

import io.crnk.jpa.query.BasicQueryTestBase;
import io.crnk.jpa.query.JpaQueryFactory;

import javax.persistence.EntityManager;

public class BasicQuerydslTest extends BasicQueryTestBase {

	@Override
	protected JpaQueryFactory createQueryFactory(EntityManager em) {
		return QuerydslQueryFactory.newInstance();
	}
}
