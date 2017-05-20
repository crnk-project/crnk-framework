package io.crnk.jpa.internal.query.backend.querydsl;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.QTuple;
import com.querydsl.jpa.impl.JPAQuery;
import io.crnk.jpa.internal.query.AbstractQueryExecutorImpl;
import io.crnk.jpa.query.querydsl.QuerydslExecutor;
import io.crnk.jpa.query.querydsl.QuerydslTuple;
import io.crnk.meta.model.MetaDataObject;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class QuerydslExecutorImpl<T> extends AbstractQueryExecutorImpl<T> implements QuerydslExecutor<T> {

	private JPAQuery<T> query;

	public QuerydslExecutorImpl(EntityManager em, MetaDataObject meta, JPAQuery<T> query, int numAutoSelections,
								Map<String, Integer> selectionBindings) {
		super(em, meta, numAutoSelections, selectionBindings);

		this.query = query;
	}

	@Override
	public JPAQuery<T> getQuery() {
		return query;
	}

	@Override
	public void setQuery(JPAQuery<T> query) {
		this.query = query;
	}

	@SuppressWarnings("unchecked")
	@Override
	public TypedQuery<T> getTypedQuery() {
		return (TypedQuery<T>) setupQuery(query.createQuery());
	}

	@Override
	protected boolean isCompoundSelection() {
		return query.getMetadata().getProjection() instanceof QTuple;
	}

	@Override
	protected boolean isDistinct() {
		return query.getMetadata().isDistinct();
	}

	@Override
	protected boolean hasManyRootsFetchesOrJoins() {
		return QuerydslUtils.hasManyRootsFetchesOrJoins(query);
	}

	/**
	 * Returns the row count for the query.
	 */
	@Override
	public long getTotalRowCount() {
		return query.fetchCount();
	}

	@Override
	public List<QuerydslTuple> getResultTuples() {
		List<?> results = executeQuery();

		List<QuerydslTuple> tuples = new ArrayList<>();
		for (Object result : results) {
			if (result instanceof Tuple) {
				tuples.add(new QuerydslTupleImpl((Tuple) result, selectionBindings));
			} else {
				tuples.add(new QuerydslObjectArrayTupleImpl(result, selectionBindings));
			}
		}
		return tuples;
	}
}
