package io.crnk.operations.model;

import java.io.Serializable;
import java.util.Set;
import java.util.UUID;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;

@Entity
@Table(indexes = {@Index(columnList = "name")})
public class PersonEntity implements Serializable {

	@Id
	private UUID id;

	@Version
	private Long version;

	@NotNull
	private String name;

	@ManyToMany(mappedBy = "directors")
	private Set<MovieEntity> directedMovies;

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public Set<MovieEntity> getDirectedMovies() {
		return directedMovies;
	}

	public void setDirectedMovies(Set<MovieEntity> directedMovies) {
		this.directedMovies = directedMovies;
	}

	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
