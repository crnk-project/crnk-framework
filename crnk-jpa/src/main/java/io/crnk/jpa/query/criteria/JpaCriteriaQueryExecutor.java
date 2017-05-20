package io.crnk.jpa.query.criteria;

import io.crnk.jpa.query.JpaQueryExecutor;

import javax.persistence.Tuple;
import java.util.List;

public interface JpaCriteriaQueryExecutor<T> extends JpaQueryExecutor<T> {

	/**
	 * @return tuple when doing a custom selection.
	 */
	@SuppressWarnings("unchecked")
	@Override
	List<Tuple> getResultTuples();

}
