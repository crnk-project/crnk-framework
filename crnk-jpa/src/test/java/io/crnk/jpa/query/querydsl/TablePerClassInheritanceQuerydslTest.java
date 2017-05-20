package io.crnk.jpa.query.querydsl;

import io.crnk.jpa.query.JpaQueryFactory;
import io.crnk.jpa.query.TablePerClassInhertitanceQueryTestBase;

import javax.persistence.EntityManager;

public class TablePerClassInheritanceQuerydslTest extends TablePerClassInhertitanceQueryTestBase {

	@Override
	protected JpaQueryFactory createQueryFactory(EntityManager em) {
		return QuerydslQueryFactory.newInstance();
	}
}
