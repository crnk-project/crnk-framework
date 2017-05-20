package io.crnk.jpa.query.criteria;

import io.crnk.jpa.query.JpaQueryFactory;
import io.crnk.jpa.query.TablePerClassInhertitanceQueryTestBase;

import javax.persistence.EntityManager;

public class TablePerClassInheritanceCriteriaTest extends TablePerClassInhertitanceQueryTestBase {

	@Override
	protected JpaQueryFactory createQueryFactory(EntityManager em) {
		return JpaCriteriaQueryFactory.newInstance();
	}
}
