package io.crnk.data.jpa.model;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

@Entity
public class TestEmbeddedIdEntity {

	public static final String ATTR_id = "id";

	public static final String ATTR_longValue = "longValue";

	@EmbeddedId
	private TestIdEmbeddable id;

	@Column
	private long longValue;

	public TestIdEmbeddable getId() {
		return id;
	}

	public void setId(TestIdEmbeddable id) {
		this.id = id;
	}

	public long getLongValue() {
		return longValue;
	}

	public void setLongValue(long longValue) {
		this.longValue = longValue;
	}
}
