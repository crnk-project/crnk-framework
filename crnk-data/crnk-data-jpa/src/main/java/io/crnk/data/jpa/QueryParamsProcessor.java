package io.crnk.data.jpa;

import io.crnk.data.jpa.query.JpaQuery;
import io.crnk.data.jpa.query.JpaQueryExecutor;
import io.crnk.legacy.queryParams.QueryParams;

public interface QueryParamsProcessor {

	<T> void prepareExecution(JpaQueryExecutor<T> executor, QueryParams queryParams);

	<T> void prepareQuery(JpaQuery<T> builder, QueryParams queryParams);

}
