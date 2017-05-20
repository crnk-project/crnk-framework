package io.crnk.jpa.query.querydsl;

import io.crnk.jpa.query.EmbeddableIdQueryTestBase;
import io.crnk.jpa.query.JpaQueryFactory;

import javax.persistence.EntityManager;

public class EmbeddableIdQuerydslTest extends EmbeddableIdQueryTestBase {

	@Override
	protected JpaQueryFactory createQueryFactory(EntityManager em) {
		return QuerydslQueryFactory.newInstance();
	}

}
