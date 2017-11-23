package io.crnk.jpa.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class FieldOnlyEntity {

	public static final String ATTR_id = "id";

	public static final String ATTR_longValue = "longValue";

	@Id
	public Long id;

	@Column
	public long longValue;
}