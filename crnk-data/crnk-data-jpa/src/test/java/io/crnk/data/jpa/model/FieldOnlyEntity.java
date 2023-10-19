package io.crnk.data.jpa.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class FieldOnlyEntity {

	public static final String ATTR_id = "id";

	public static final String ATTR_longValue = "longValue";

	@Id
	public Long id;

	@Column
	public long longValue;
}