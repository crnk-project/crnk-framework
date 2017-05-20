package io.crnk.jpa.query.querydsl;

import io.crnk.jpa.query.JpaQuery;

public interface QuerydslQuery<T> extends JpaQuery<T> {

	@Override
	QuerydslExecutor<T> buildExecutor();
}
