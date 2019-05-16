package io.crnk.data.jpa.model;

import javax.persistence.Entity;

@Entity
public class AnnotationMappedSubtypeEntity extends AnnotationMappedSuperclassEntity {

	private String subtypeValue;

	public String getSubtypeValue() {
		return subtypeValue;
	}

	public void setSubtypeValue(String subtypeValue) {
		this.subtypeValue = subtypeValue;
	}
}
