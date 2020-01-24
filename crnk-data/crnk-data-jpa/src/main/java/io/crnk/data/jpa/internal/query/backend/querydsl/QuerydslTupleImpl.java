package io.crnk.data.jpa.internal.query.backend.querydsl;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import io.crnk.data.jpa.query.querydsl.QuerydslTuple;

import java.util.Arrays;
import java.util.Map;

public class QuerydslTupleImpl implements QuerydslTuple {

	private Tuple tuple;

	private Map<String, Integer> selectionBindings;

	private int numEntriesToIgnore;

	public QuerydslTupleImpl(Tuple tuple, Map<String, Integer> selectionBindings) {
		this.tuple = tuple;
		this.selectionBindings = selectionBindings;
	}

	@Override
	public <T> T get(int index, Class<T> type) {
		return tuple.get(index + numEntriesToIgnore, type);
	}

	@Override
	public <T> T get(Expression<T> expr) {
		return tuple.get(expr);
	}

	@Override
	public int size() {
		return tuple.size() - numEntriesToIgnore;
	}

	@Override
	public Object[] toArray() {
		Object[] data = tuple.toArray();
		if (numEntriesToIgnore > 0) {
			return Arrays.copyOfRange(data, numEntriesToIgnore, data.length);
		} else {
			return data;
		}
	}

	@Override
	public <T> T get(String name, Class<T> clazz) {
		Integer index = selectionBindings.get(name);
		if (index == null) {
			throw new IllegalArgumentException("selection " + name + " not found");
		}
		return get(index.intValue(), clazz);
	}

	@Override
	public void reduce(int numEntriesToIgnore) {
		this.numEntriesToIgnore = numEntriesToIgnore;
	}

}
