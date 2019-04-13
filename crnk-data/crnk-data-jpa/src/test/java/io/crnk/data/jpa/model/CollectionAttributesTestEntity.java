package io.crnk.data.jpa.model;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.List;

@Entity
public class CollectionAttributesTestEntity {

	public static final String ATTR_id = "id";


	public static final String ATTR_stringValues = "stringValues";

	public static final String ATTR_longValues = "longValues";

	@Id
	private Long id;

	@ElementCollection
	private List<Long> longValues;

	@ElementCollection
	private List<String> stringValues;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public List<Long> getLongValues() {
		return longValues;
	}

	public void setLongValues(List<Long> longValues) {
		this.longValues = longValues;
	}

	public List<String> getStringValues() {
		return stringValues;
	}

	public void setStringValues(List<String> stringValues) {
		this.stringValues = stringValues;
	}

}
