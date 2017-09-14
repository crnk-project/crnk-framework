package io.crnk.jpa.model;

import java.util.Set;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;

@Entity
public class ManyToManyTestEntity {

	@Id
	private Long id;

	@ManyToMany
	private Set<ManyToManyOppositeEntity> opposites;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Set<ManyToManyOppositeEntity> getOpposites() {
		return opposites;
	}

	public void setOpposites(Set<ManyToManyOppositeEntity> opposites) {
		this.opposites = opposites;
	}
}
