package io.crnk.operations.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;


@Entity
public class VoteEntity {

	@Id
	@SequenceGenerator(name = "vote_id", allocationSize = 1, sequenceName = "vote_id")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "vote_id")
	private Long id;

	private int numStars;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public int getNumStars() {
		return numStars;
	}

	public void setNumStars(int numStars) {
		this.numStars = numStars;
	}
}
