package io.crnk.jpa.query.criteria;

import io.crnk.jpa.query.JpaQuery;

public interface JpaCriteriaQuery<T> extends JpaQuery<T> {

	@Override
	JpaCriteriaQueryExecutor<T> buildExecutor();
}
