package io.crnk.data.jpa.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class TestNestedEmbeddable {

	public static final String ATTR_embBoolValue = "embBoolValue";

	@Column
	private Boolean embBoolValue;

	public TestNestedEmbeddable() {
	}

	public TestNestedEmbeddable(boolean embBoolValue) {
		this.embBoolValue = embBoolValue;
	}

	public Boolean getEmbBoolValue() {
		return embBoolValue;
	}

	public void setEmbBoolValue(Boolean embBoolValue) {
		this.embBoolValue = embBoolValue;
	}
}
