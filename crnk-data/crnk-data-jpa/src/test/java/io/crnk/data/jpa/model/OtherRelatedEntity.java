package io.crnk.data.jpa.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

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