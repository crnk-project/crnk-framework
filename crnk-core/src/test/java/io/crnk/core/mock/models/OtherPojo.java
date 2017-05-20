package io.crnk.core.mock.models;

import java.util.Objects;

public class OtherPojo {
	private String value;

	public String getValue() {
		return value;
	}

	public OtherPojo setValue(String value) {
		this.value = value;
		return this;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		OtherPojo otherPojo = (OtherPojo) o;
		return Objects.equals(value, otherPojo.value);
	}

	@Override
	public int hashCode() {
		return Objects.hash(value);
	}
}
