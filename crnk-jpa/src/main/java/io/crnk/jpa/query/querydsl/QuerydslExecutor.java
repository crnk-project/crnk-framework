package io.crnk.jpa.query.querydsl;

import com.querydsl.jpa.impl.JPAQuery;
import io.crnk.jpa.query.JpaQueryExecutor;

import java.util.List;

public interface QuerydslExecutor<T> extends JpaQueryExecutor<T> {

	@Override
	List<QuerydslTuple> getResultTuples();

	JPAQuery<T> getQuery();

	void setQuery(JPAQuery<T> query);
}
