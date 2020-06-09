package io.crnk.core.engine.internal.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.crnk.core.queryspec.PathSpec;

/**
 * @param <K> key
 * @param <V> value
 */
public class MultivaluedMap<K, V> {

	private Map<K, List<V>> map = new HashMap<>();

	public static <K, V> MultivaluedMap<K, V> fromCollection(Collection<V> values, PathSpec keyPath) {
		MultivaluedMap<K, V> map = new MultivaluedMap<>();
		for (V value : values) {
			K key = (K) PropertyUtils.getProperty(value, keyPath.getElements());
			PreconditionUtil.verify(key != null, "key must not be null for {}", value);
			map.add(key, value);
		}
		return map;
	}

	public void add(K key, V value) {
		List<V> list = map.get(key);
		if (list == null) {
			list = newList();
			map.put(key, list);
		}
		list.add(value);
	}

	protected List<V> newList() {
		return new ArrayList<>();
	}

	public void addAll(K key, Iterable<V> values) {
		for (V value : values) {
			add(key, value);
		}
	}

	public boolean isEmpty() {
		return map.isEmpty();
	}

	public boolean containsKey(K key) {
		return map.containsKey(key);
	}

	public V getUnique(K key) {
		return getUnique(key, false);
	}

	public V getUnique(K key, boolean nullable) {
		List<V> list = map.get(key);
		if (nullable && (list == null || list.isEmpty())) {
			return null;
		}
		if (list.size() > 1) {
			throw new IllegalStateException("expected unique key=" + key + ", got=" + list);
		}
		return list.get(0);
	}

	public List<V> getList(K key) {
		List<V> list = map.get(key);
		PreconditionUtil.verify(list != null, "key=%s not available", key);
		return list;
	}

	public Set<K> keySet() {
		return map.keySet();
	}

	public Collection<List<V>> values() {
		return map.values();
	}

	public void set(K key, List<V> values) {
		map.put(key, values);
	}
}
