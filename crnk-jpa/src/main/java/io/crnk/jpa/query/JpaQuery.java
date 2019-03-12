package io.crnk.jpa.query;

import java.util.List;
import javax.persistence.criteria.JoinType;

import io.crnk.core.queryspec.Direction;
import io.crnk.core.queryspec.FilterOperator;
import io.crnk.core.queryspec.FilterSpec;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.queryspec.SortSpec;

public interface JpaQuery<T> {

	JpaQuery<T> setEnsureTotalOrder(boolean ensureTotalOrder);

	JpaQuery<T> addFilter(FilterSpec filters);

	JpaQuery<T> addSortBy(List<String> path, Direction dir);

	JpaQuery<T> addSortBy(SortSpec order);

	JpaQuery<T> setDefaultJoinType(JoinType joinType);

	JpaQuery<T> setJoinType(List<String> path, JoinType joinType);

	JpaQuery<T> setAutoGroupBy(boolean autoGroupBy);

	JpaQuery<T> setDistinct(boolean distinct);

	JpaQuery<T> addFilter(List<String> attrPath, FilterOperator operator, Object value);

	JpaQuery<T> addFilter(String attrPath, FilterOperator operator, Object value);

	JpaQueryExecutor<T> buildExecutor();

	JpaQueryExecutor<T> buildExecutor(QuerySpec querySpec);

	Class<T> getEntityClass();

	void addSelection(List<String> path);

	void addParentIdSelection();

	/**
	 * @return private data that can be set by the consumer to provide some context for a query, for example, when being called back by an interceptor. Does
	 * not have any direct impact on the created query.
	 */
	Object getPrivateData();

	void setPrivateData(Object privateData);

}
