package io.crnk.data.jpa.internal.query.backend.querydsl;

import io.crnk.data.jpa.query.criteria.JpaCriteriaTuple;

import jakarta.persistence.TupleElement;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ObjectArrayTupleImpl implements JpaCriteriaTuple {

	private Object[] data;

	private int numEntriesToIgnore;

	private Map<String, Integer> selectionBindings;

	public ObjectArrayTupleImpl(Object entity, Map<String, Integer> selectionBindings) {
		this.selectionBindings = selectionBindings;
		if (entity instanceof Object[]) {
			data = (Object[]) entity;
		} else {
			data = new Object[]{entity};
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(int index, Class<T> type) {
		return (T) data[index + numEntriesToIgnore];
	}

	public int size() {
		return data.length - numEntriesToIgnore;
	}

	@Override
	public Object[] toArray() {
		if (numEntriesToIgnore > 0) {
			return Arrays.copyOfRange(data, numEntriesToIgnore, data.length);
		} else {
			return data;
		}
	}

	@Override
	public <T> T get(String name, Class<T> clazz) {
		int index = selectionBindings.get(name);
		return get(index, clazz);
	}

	@Override
	public <X> X get(TupleElement<X> element) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object get(String name) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object get(int index) {
		return get(index, Object.class);
	}

	@Override
	public List<TupleElement<?>> getElements() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void reduce(int numEntriesToIgnore) {
		this.numEntriesToIgnore = numEntriesToIgnore;
	}

}
