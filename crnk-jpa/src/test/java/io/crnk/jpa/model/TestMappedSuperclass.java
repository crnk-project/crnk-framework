package io.crnk.jpa.model;

import javax.persistence.*;

@MappedSuperclass
public class TestMappedSuperclass {

	public static final String ATTR_superRelatedValue = "superRelatedValue";

	@Column
	private String stringValue;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn
	private RelatedEntity superRelatedValue;

	public String getStringValue() {
		return stringValue;
	}

	public void setStringValue(String stringValue) {
		this.stringValue = stringValue;
	}

	public RelatedEntity getSuperRelatedValue() {
		return superRelatedValue;
	}

	public void setSuperRelatedValue(RelatedEntity superRelatedValue) {
		this.superRelatedValue = superRelatedValue;
	}
}
