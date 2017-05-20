package io.crnk.legacy.queryParams.params;

import io.crnk.legacy.queryParams.RestrictedSortingValues;

import java.util.HashMap;
import java.util.Map;

public class SortingParams {
	private Map<String, RestrictedSortingValues> params = new HashMap<>();

	public SortingParams(Map<String, RestrictedSortingValues> params) {
		this.params = params;
	}

	public Map<String, RestrictedSortingValues> getParams() {
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

		SortingParams that = (SortingParams) o;

		return params != null ? params.equals(that.params) : that.params == null;
	}

	@Override
	public String toString() {
		return "SortingParams{" +
				"params=" + params +
				'}';
	}
}