package io.crnk.data.jpa.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity
public class TestSubclassWithSuperclassPk extends TestMappedSuperclassWithPk {

	@Column
	private long longValue;

	public long getLongValue() {
		return longValue;
	}

	public void setLongValue(long longValue) {
		this.longValue = longValue;
	}
}
