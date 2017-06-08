package io.crnk.jpa.query;

import java.util.List;

public interface JpaQueryExecutor<T> {

	/**
	 * @return Count the number of objects returned without any paging applied.
	 */
	long getTotalRowCount();

	T getUniqueResult(boolean nullable);

	List<T> getResultList();

	JpaQueryExecutor<T> setLimit(int limit);

	JpaQueryExecutor<T> setOffset(int offset);

	JpaQueryExecutor<T> setWindow(int offset, int limit);

	JpaQueryExecutor<T> setCached(boolean cached);

	JpaQueryExecutor<T> fetch(List<String> attrPath);

	Class<T> getEntityClass();

	<U extends Tuple> List<U> getResultTuples();

	int getLimit();
}
