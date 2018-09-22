package io.crnk.jpa.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class OtherRelatedEntity {

	public static final String ATTR_id = "id";

	public static final String ATTR_stringValue = "stringValue";
	@Id
	private Long id;
	@Column
	private String stringValue;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn
	private OtherRelatedEntity otherEntity;

	public OtherRelatedEntity() {

	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getStringValue() {
		return stringValue;
	}

	public void setStringValue(String stringValue) {
		this.stringValue = stringValue;
	}

	public OtherRelatedEntity getOtherEntity() {
		return otherEntity;
	}

	public void setOtherEntity(OtherRelatedEntity otherEntity) {
		this.otherEntity = otherEntity;
	}
}