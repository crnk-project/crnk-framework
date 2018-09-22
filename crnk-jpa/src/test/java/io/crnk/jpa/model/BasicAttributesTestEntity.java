package io.crnk.jpa.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import java.time.OffsetDateTime;

@Entity
public class BasicAttributesTestEntity {

	public static final String ATTR_id = "id";

	public static final String ATTR_stringValue = "stringValue";

	public static final String ATTR_longValue = "longValue";

	public static final String ATTR_enumValue = "enumValue";

	public static final String ATTR_offsetDateTimeValue = "offsetDateTimeValue";

	@Id
	private Long id;

	@Column
	private long longValue;

	@Column
	private Long nullableLongValue;

	@Column
	private Boolean booleanValue;

	@Column
	private Boolean nullableBooleanValue;

	@Enumerated(EnumType.STRING)
	private TestEnum enumValue;

	private OffsetDateTime offsetDateTimeValue;

	public OffsetDateTime getOffsetDateTimeValue() {
		return offsetDateTimeValue;
	}

	public void setOffsetDateTimeValue(OffsetDateTime offsetDateTimeValue) {
		this.offsetDateTimeValue = offsetDateTimeValue;
	}

	public TestEnum getEnumValue() {
		return enumValue;
	}

	public void setEnumValue(TestEnum enumValue) {
		this.enumValue = enumValue;
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

	public Long getNullableLongValue() {
		return nullableLongValue;
	}

	public void setNullableLongValue(Long nullableLongValue) {
		this.nullableLongValue = nullableLongValue;
	}

	public Boolean getBooleanValue() {
		return booleanValue;
	}

	public void setBooleanValue(Boolean booleanValue) {
		this.booleanValue = booleanValue;
	}

	public Boolean getNullableBooleanValue() {
		return nullableBooleanValue;
	}

	public void setNullableBooleanValue(Boolean nullableBooleanValue) {
		this.nullableBooleanValue = nullableBooleanValue;
	}
}
