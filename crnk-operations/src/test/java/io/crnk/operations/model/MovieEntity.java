package io.crnk.operations.model;

import java.util.Set;
import java.util.UUID;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;


@Entity
public class MovieEntity extends MovieInfo {

	@Id
	private UUID id;

	@ManyToMany
	private Set<PersonEntity> directors;

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public Set<PersonEntity> getDirectors() {
		return directors;
	}

	public void setDirectors(Set<PersonEntity> directors) {
		this.directors = directors;
	}
}
