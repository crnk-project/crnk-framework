package io.crnk.jpa.repository.querydsl;

import io.crnk.jpa.query.JpaQueryFactory;
import io.crnk.jpa.query.querydsl.QuerydslQueryFactory;
import io.crnk.jpa.repository.JpaEntityRepositoryTestBase;

import javax.persistence.EntityManager;

public class JpaEntityRepositoryQuerydslTest extends JpaEntityRepositoryTestBase {

	@Override
	protected JpaQueryFactory createQueryFactory(EntityManager em) {
		return QuerydslQueryFactory.newInstance();
	}

}
