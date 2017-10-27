package io.crnk.jpa.model;

import io.crnk.core.resource.annotations.JsonApiField;
import io.crnk.core.resource.annotations.JsonApiResource;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;

@JsonApiResource(type = "annotation-test")
@Entity
public class AnnotationTestEntity {

	@Id
	private Long id;

	@Lob
	@Column
	private String lobValue;

	@JsonApiField(sortable = false, filterable = false, patchable = false, postable = true)
	@Column
	private String fieldAnnotatedValue;

	@Column(insertable = false, updatable = true)
	private String columnAnnotatedValue;

	@Column(nullable = false)
	private String notNullableValue;

	@Column(nullable = true)
	private String nullableValue;

	@ManyToOne(fetch = FetchType.EAGER, optional = false)
	private RelatedEntity nonOptionalRelatedValue;

	@ManyToOne(fetch = FetchType.EAGER, optional = true)
	private RelatedEntity optionalRelatedValue;

	private String readOnlyValue = "someReadOnlyValue";

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getReadOnlyValue() {
		return readOnlyValue;
	}

	public String getLobValue() {
		return lobValue;
	}

	public void setLobValue(String lobValue) {
		this.lobValue = lobValue;
	}

	public String getFieldAnnotatedValue() {
		return fieldAnnotatedValue;
	}

	public void setFieldAnnotatedValue(String attributeAnnotatedValue) {
		this.fieldAnnotatedValue = attributeAnnotatedValue;
	}

	public String getColumnAnnotatedValue() {
		return columnAnnotatedValue;
	}

	public void setColumnAnnotatedValue(String columnAnnotatedValue) {
		this.columnAnnotatedValue = columnAnnotatedValue;
	}

	public String getNotNullableValue() {
		return notNullableValue;
	}

	public void setNotNullableValue(String notNullableValue) {
		this.notNullableValue = notNullableValue;
	}

	public String getNullableValue() {
		return nullableValue;
	}

	public void setNullableValue(String nullableValue) {
		this.nullableValue = nullableValue;
	}

	public RelatedEntity getNonOptionalRelatedValue() {
		return nonOptionalRelatedValue;
	}

	public void setNonOptionalRelatedValue(RelatedEntity nonOptionalRelatedValue) {
		this.nonOptionalRelatedValue = nonOptionalRelatedValue;
	}

	public RelatedEntity getOptionalRelatedValue() {
		return optionalRelatedValue;
	}

	public void setOptionalRelatedValue(RelatedEntity optionalRelatedValue) {
		this.optionalRelatedValue = optionalRelatedValue;
	}
}
