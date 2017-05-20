package io.crnk.jpa.model;

import io.crnk.core.resource.annotations.JsonApiField;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public class AnnotationMappedSuperclassEntity {

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

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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
}
