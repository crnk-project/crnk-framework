package io.crnk.data.jpa.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class NamingTestEntity {

	@Id
	private Long id;
	private Long sEcondUpperCaseValue;

	public Long getSEcondUpperCaseValue() {
		return sEcondUpperCaseValue;
	}

	public void setSEcondUpperCaseValue(Long sEcondUpperCaseValue) {
		this.sEcondUpperCaseValue = sEcondUpperCaseValue;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
}
