package io.crnk.data.jpa.query.criteria;

import io.crnk.data.jpa.query.JpaQueryFactory;
import io.crnk.data.jpa.query.TablePerClassInhertitanceQueryTestBase;

import jakarta.persistence.EntityManager;

public class TablePerClassInheritanceCriteriaTest extends TablePerClassInhertitanceQueryTestBase {

	@Override
	protected JpaQueryFactory createQueryFactory(EntityManager em) {
		return JpaCriteriaQueryFactory.newInstance();
	}
}
