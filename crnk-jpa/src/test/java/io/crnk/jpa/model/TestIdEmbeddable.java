package io.crnk.jpa.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class TestIdEmbeddable implements Serializable {

	public static final String ATTR_embIntValue = "embIntValue";
	public static final String ATTR_embStringValue = "embStringValue";
	private static final long serialVersionUID = 4473954915317129238L;
	@Column
	private Integer embIntValue;

	@Column
	private String embStringValue;

	public TestIdEmbeddable() {
	}

	public TestIdEmbeddable(Integer intValue, String stringValue) {
		this.embIntValue = intValue;
		this.embStringValue = stringValue;
	}

	public Integer getEmbIntValue() {
		return embIntValue;
	}

	public void setEmbIntValue(Integer embIntValue) {
		this.embIntValue = embIntValue;
	}

	public String getEmbStringValue() {
		return embStringValue;
	}

	public void setEmbStringValue(String embStringValue) {
		this.embStringValue = embStringValue;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((embIntValue == null) ? 0 : embIntValue.hashCode());
		result = prime * result + ((embStringValue == null) ? 0 : embStringValue.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TestIdEmbeddable other = (TestIdEmbeddable) obj;
		if (embIntValue == null) {
			if (other.embIntValue != null)
				return false;
		} else if (!embIntValue.equals(other.embIntValue))
			return false;
		if (embStringValue == null) {
			return other.embStringValue == null;
		} else return embStringValue.equals(other.embStringValue);
	}
}
