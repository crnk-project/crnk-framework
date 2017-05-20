package io.crnk.jpa.model;

import javax.persistence.*;

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
