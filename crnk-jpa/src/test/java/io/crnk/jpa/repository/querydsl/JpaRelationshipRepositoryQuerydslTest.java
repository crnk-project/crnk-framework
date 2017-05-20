package io.crnk.jpa.repository.querydsl;

import io.crnk.jpa.query.JpaQueryFactory;
import io.crnk.jpa.query.querydsl.QuerydslQueryFactory;
import io.crnk.jpa.repository.JpaRelationshipRepositoryTestBase;

import javax.persistence.EntityManager;

public class JpaRelationshipRepositoryQuerydslTest extends JpaRelationshipRepositoryTestBase {

	@Override
	protected JpaQueryFactory createQueryFactory(EntityManager em) {
		return QuerydslQueryFactory.newInstance();
	}
}
