package io.crnk.data.jpa.query.criteria;

import io.crnk.data.jpa.query.JpaQueryExecutor;

import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaQuery;
import java.util.List;

public interface JpaCriteriaQueryExecutor<T> extends JpaQueryExecutor<T> {

	/**
	 * @return tuple when doing a custom selection.
	 */
	@SuppressWarnings("unchecked")
	@Override
	List<Tuple> getResultTuples();

	CriteriaQuery getQuery();
}
