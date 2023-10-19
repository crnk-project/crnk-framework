package io.crnk.data.jpa.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Version;

@Entity
public class VersionedEntity extends TestMappedSuperclass {

	public static final String ATTR_id = "id";

	public static final String ATTR_longValue = "longValue";

	@Id
	private Long id;

	@Column
	private long longValue;

	@Version
	@Column
	private int version = -1;

	public VersionedEntity() {

	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public long getLongValue() {
		return longValue;
	}

	public void setLongValue(long longValue) {
		this.longValue = longValue;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}
}
