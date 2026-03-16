package io.crnk.data.jpa.query.querydsl;

import io.crnk.data.jpa.query.JpaQueryFactory;
import io.crnk.data.jpa.query.TablePerClassInhertitanceQueryTestBase;

import javax.persistence.EntityManager;

public class TablePerClassInheritanceQuerydslTest extends TablePerClassInhertitanceQueryTestBase {

	@Override
	protected JpaQueryFactory createQueryFactory(EntityManager em) {
		return QuerydslQueryFactory.newInstance();
	}
}
