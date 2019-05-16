package io.crnk.data.jpa.model;

import javax.persistence.Column;
import javax.persistence.Entity;

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
