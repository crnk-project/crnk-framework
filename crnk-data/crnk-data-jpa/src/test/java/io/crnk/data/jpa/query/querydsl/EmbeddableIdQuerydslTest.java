package io.crnk.data.jpa.query.querydsl;

import io.crnk.data.jpa.query.EmbeddableIdQueryTestBase;
import io.crnk.data.jpa.query.JpaQueryFactory;

import javax.persistence.EntityManager;

public class EmbeddableIdQuerydslTest extends EmbeddableIdQueryTestBase {

	@Override
	protected JpaQueryFactory createQueryFactory(EntityManager em) {
		return QuerydslQueryFactory.newInstance();
	}

}
