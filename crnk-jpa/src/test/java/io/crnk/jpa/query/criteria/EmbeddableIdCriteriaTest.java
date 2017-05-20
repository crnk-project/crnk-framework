package io.crnk.jpa.query.criteria;

import io.crnk.jpa.query.EmbeddableIdQueryTestBase;
import io.crnk.jpa.query.JpaQueryFactory;

import javax.persistence.EntityManager;

public class EmbeddableIdCriteriaTest extends EmbeddableIdQueryTestBase {

	@Override
	protected JpaQueryFactory createQueryFactory(EntityManager em) {
		return JpaCriteriaQueryFactory.newInstance();
	}

}
