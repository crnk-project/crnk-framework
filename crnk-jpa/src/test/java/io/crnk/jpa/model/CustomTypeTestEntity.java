package io.crnk.jpa.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;

@Entity
public class CustomTypeTestEntity {

	// this is not an Embeddable!
	public static class CustomType implements Serializable {

		private String value;

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}
	}

	public static final String ATTR_id = "id";

	public static final String ATTR_value = "value";

	@Id
	private Long id;

	@Column
	private CustomType value;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public CustomType getValue() {
		return value;
	}

	public void setValue(CustomType value) {
		this.value = value;
	}
}
