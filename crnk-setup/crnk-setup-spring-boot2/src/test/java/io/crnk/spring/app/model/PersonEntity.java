package io.crnk.spring.app.model;

import io.crnk.data.facet.annotation.Facet;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class PersonEntity {

	@Id
	private Long id;

	@Facet
	private String name;

	@Facet
	private int birthYear;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getBirthYear() {
		return birthYear;
	}

	public void setBirthYear(int birthYear) {
		this.birthYear = birthYear;
	}
}
