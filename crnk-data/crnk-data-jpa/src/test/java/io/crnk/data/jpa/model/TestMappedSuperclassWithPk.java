package io.crnk.data.jpa.model;

import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public class TestMappedSuperclassWithPk {

	public static final String ATTR_superRelatedValue = "superRelatedValue";

	@Column
	@Id
	private String id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn
	private RelatedEntity superRelatedValue;

	public String getId() {
		return id;
	}

	public void setId(String stringValue) {
		this.id = stringValue;
	}

	public RelatedEntity getSuperRelatedValue() {
		return superRelatedValue;
	}

	public void setSuperRelatedValue(RelatedEntity superRelatedValue) {
		this.superRelatedValue = superRelatedValue;
	}

}
