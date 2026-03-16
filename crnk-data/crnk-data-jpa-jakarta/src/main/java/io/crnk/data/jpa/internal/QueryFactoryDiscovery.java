package io.crnk.data.jpa.internal;

import io.crnk.core.engine.internal.utils.ClassUtils;
import io.crnk.data.jpa.query.JpaQueryFactory;
import io.crnk.data.jpa.query.criteria.JpaCriteriaQueryFactory;
import io.crnk.data.jpa.query.querydsl.QuerydslQueryFactory;

/**
 * Checks for the presence of QueryDSL and makes use of it. Otherwise fallsback
 * to Criteria aPI.
 */
public class QueryFactoryDiscovery {


	public JpaQueryFactory discoverDefaultFactory() {
		if (ClassUtils.existsClass("com.querydsl.jpa.impl.JPAQuery")) {
			return QuerydslQueryFactory.newInstance();
		} else {
			return JpaCriteriaQueryFactory.newInstance();
		}
	}
}
