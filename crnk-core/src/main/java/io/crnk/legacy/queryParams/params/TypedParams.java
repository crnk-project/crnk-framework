package io.crnk.legacy.queryParams.params;

import java.util.HashMap;
import java.util.Map;

/**
 * Generic query parameter container
 *
 * @param <T> type of the parameter
 */
public class TypedParams<T> {
	private Map<String, T> params = new HashMap<>();

	public TypedParams(Map<String, T> params) {
		this.params = params;
	}

	public Map<String, T> getParams() {
		return params;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		TypedParams<?> that = (TypedParams<?>) o;

		return params != null ? params.equals(that.params) : that.params == null;
	}

	@Override
	public int hashCode() {
		return params != null ? params.hashCode() : 0;
	}

	@Override
	public String toString() {
		return "TypedParams{" +
				"params=" + params +
				'}';
	}
}