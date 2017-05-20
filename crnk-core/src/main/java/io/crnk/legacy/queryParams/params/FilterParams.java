package io.crnk.legacy.queryParams.params;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class FilterParams {
	private Map<String, Set<String>> params = new HashMap<>();

	public FilterParams(Map<String, Set<String>> params) {
		this.params = params;
	}

	public Map<String, Set<String>> getParams() {
		return params;
	}

	@Override
	public int hashCode() {
		return params != null ? params.hashCode() : 0;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		FilterParams that = (FilterParams) o;

		return params != null ? params.equals(that.params) : that.params == null;
	}

	@Override
	public String toString() {
		return "FilterParams{" +
				"params=" + params +
				'}';
	}
}