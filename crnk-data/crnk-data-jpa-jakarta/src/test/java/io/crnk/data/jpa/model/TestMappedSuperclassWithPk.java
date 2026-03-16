package io.crnk.data.jpa.model;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

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
