package io.crnk.data.jpa.internal.query.backend.querydsl;

import com.querydsl.core.types.Expression;
import io.crnk.data.jpa.query.querydsl.QuerydslTuple;

import java.util.Map;

public class QuerydslObjectArrayTupleImpl extends ObjectArrayTupleImpl implements QuerydslTuple {

	public QuerydslObjectArrayTupleImpl(Object entity, Map<String, Integer> selectionBindings) {
		super(entity, selectionBindings);
	}

	@Override
	public <T> T get(Expression<T> expr) {
		throw new UnsupportedOperationException();
	}
}
