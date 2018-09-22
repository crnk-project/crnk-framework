package io.crnk.jpa.model;

import io.crnk.core.resource.annotations.JsonApiId;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class OverrideIdTestEntity extends TestMappedSuperclass {

	public static final String ATTR_id = "id";

	public static final String ATTR_pk = "pk";

	@Id
	private Long pk;

	@JsonApiId
	private Long id;

	private String value;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getPk() {
		return pk;
	}

	public void setPk(Long pk) {
		this.pk = pk;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
