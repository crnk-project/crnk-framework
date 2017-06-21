package io.crnk.jpa.model;

import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiResource;

import javax.persistence.*;

@Entity
@JsonApiResource("tablePerClassBase")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class TablePerClassBaseEntity {

	public static final String ATTR_id = "id";

	public static final String ATTR_stringValue = "stringValue";

	@Id
	@JsonApiId
	private Long id;

	@Column
	private String stringValue;

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

}