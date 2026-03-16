package io.crnk.data.jpa.model;

import io.crnk.data.jpa.query.AnyTypeObject;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class TestAnyType implements AnyTypeObject {

	@Column
	private String type;

	@Column
	private String stringValue;

	@Column
	private Integer intValue;

	@Override
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public Object getValue() {
		if ("intValue".equals(type))
			return intValue;
		else if ("stringValue".equals(type))
			return stringValue;
		return null;
	}

	@Override
	public void setValue(Object value) {
		if (value == null) {
			intValue = null;
			stringValue = null;
			type = null;
		} else if (value instanceof String) {
			intValue = null;
			stringValue = (String) value;
			type = "stringValue";
		} else {
			intValue = (Integer) value;
			stringValue = null;
			type = "intValue";
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getValue(Class<T> clazz) {
		return (T) getValue();
	}

	public String getStringValue() {
		return stringValue;
	}

	public void setStringValue(String stringValue) {
		this.stringValue = stringValue;
	}

	public Integer getIntValue() {
		return intValue;
	}

	public void setIntValue(Integer intValue) {
		this.intValue = intValue;
	}

}
