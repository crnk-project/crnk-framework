package io.crnk.legacy.queryParams.params;

import io.crnk.legacy.queryParams.include.Inclusion;

import java.util.Set;

public class IncludedRelationsParams {
	private Set<Inclusion> params;

	public IncludedRelationsParams(Set<Inclusion> params) {
		this.params = params;
	}

	public Set<Inclusion> getParams() {
		return params;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		IncludedRelationsParams that = (IncludedRelationsParams) o;

		return params != null ? params.equals(that.params) : that.params == null;
	}

	@Override
	public int hashCode() {
		return params != null ? params.hashCode() : 0;
	}

	@Override
	public String toString() {
		return "IncludedRelationsParams{" +
				"params=" + params +
				'}';
	}
}