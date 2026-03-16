package io.crnk.data.jpa.query.querydsl;

import io.crnk.data.jpa.query.JpaQuery;

public interface QuerydslQuery<T> extends JpaQuery<T> {

	@Override
	QuerydslExecutor<T> buildExecutor();
}
