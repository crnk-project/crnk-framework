package io.crnk.jpa.query.criteria;

import java.util.List;
import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaQuery;

import io.crnk.jpa.query.JpaQueryExecutor;

public interface JpaCriteriaQueryExecutor<T> extends JpaQueryExecutor<T> {

	/**
	 * @return tuple when doing a custom selection.
	 */
	@SuppressWarnings("unchecked")
	@Override
	List<Tuple> getResultTuples();

	CriteriaQuery getQuery();
}
