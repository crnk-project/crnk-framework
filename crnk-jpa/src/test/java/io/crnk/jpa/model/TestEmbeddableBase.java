package io.crnk.jpa.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.MappedSuperclass;

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
