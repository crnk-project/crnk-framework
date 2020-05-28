package io.crnk.data.jpa.repository.querydsl;

import io.crnk.data.jpa.query.JpaQueryFactory;
import io.crnk.data.jpa.query.querydsl.QuerydslQueryFactory;
import io.crnk.data.jpa.JpaEntityRepositoryTestBase;

import javax.persistence.EntityManager;

public class JpaEntityRepositoryQuerydslTest extends JpaEntityRepositoryTestBase {

	@Override
	protected JpaQueryFactory createQueryFactory(EntityManager em) {
		return QuerydslQueryFactory.newInstance();
	}

}
