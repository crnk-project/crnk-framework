package io.crnk.data.jpa.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.MappedSuperclass;

@Embeddable
@MappedSuperclass
public class TestEmbeddableBase {

	@Column
	private Integer embIntValue;

	public Integer getEmbIntValue() {
		return embIntValue;
	}

	public void setEmbIntValue(Integer embIntValue) {
		this.embIntValue = embIntValue;
	}
}
