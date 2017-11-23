package io.crnk.jpa.model;

import javax.persistence.Embeddable;

@Embeddable
public class AnnotationEmbeddable {

	private String embeddedValue;

	public String getEmbeddedValue() {
		return embeddedValue;
	}

	public void setEmbeddedValue(String embeddedValue) {
		this.embeddedValue = embeddedValue;
	}
}
