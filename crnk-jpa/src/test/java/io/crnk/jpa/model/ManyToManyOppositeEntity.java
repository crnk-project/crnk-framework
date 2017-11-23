package io.crnk.jpa.model;

import java.util.Set;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;

@Entity
public class ManyToManyOppositeEntity {

	@Id
	private Long id;

	@ManyToMany(mappedBy = "opposites")
	private Set<ManyToManyTestEntity> tests;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Set<ManyToManyTestEntity> getTests() {
		return tests;
	}

	public void setTests(Set<ManyToManyTestEntity> tests) {
		this.tests = tests;
	}
}
