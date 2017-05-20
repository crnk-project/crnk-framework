package io.crnk.jpa;

import io.crnk.jpa.query.JpaQuery;
import io.crnk.jpa.query.JpaQueryExecutor;
import io.crnk.legacy.queryParams.QueryParams;

public interface QueryParamsProcessor {

	<T> void prepareExecution(JpaQueryExecutor<T> executor, QueryParams queryParams);

	<T> void prepareQuery(JpaQuery<T> builder, QueryParams queryParams);

}
