package io.crnk.data.jpa.internal.query.backend.criteria;

import jakarta.persistence.Tuple;
import jakarta.persistence.TupleElement;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class CriteriaTupleImpl implements Tuple, io.crnk.data.jpa.query.Tuple {

	private Object[] data;

	private Map<String, Integer> selectionBindings;

	private int numEntriesToIgnore = 0;

	protected CriteriaTupleImpl(Object[] data, Map<String, Integer> selectionBindings) {
		this.data = data;
		this.selectionBindings = selectionBindings;
	}

	@Override
	public <X> X get(TupleElement<X> tupleElement) {
		throw new UnsupportedOperationException("not implemented");
	}

	@SuppressWarnings("unchecked")
	@Override
	public <X> X get(String alias, Class<X> type) {
		return (X) get(alias);
	}

	@Override
	public Object get(String alias) {
		Integer index = selectionBindings.get(alias);
		if (index == null) {
			throw new IllegalArgumentException("selection " + alias + " not found");
		}
		return get(index.intValue());
	}

	@SuppressWarnings("unchecked")
	@Override
	public <X> X get(int i, Class<X> type) {
		return (X) data[i + numEntriesToIgnore];
	}

	@Override
	public Object get(int i) {
		return data[i + numEntriesToIgnore];
	}

	@Override
	public Object[] toArray() {
		if (numEntriesToIgnore > 0) {
			return Arrays.copyOfRange(data, numEntriesToIgnore, data.length - numEntriesToIgnore);
		} else {
			return data;
		}
	}

	@Override
	public List<TupleElement<?>> getElements() {
		throw new UnsupportedOperationException("not implemented");
	}

	@Override
	public void reduce(int numEntriesToIgnore) {
		this.numEntriesToIgnore = numEntriesToIgnore;
	}
}
