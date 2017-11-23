package io.crnk.jpa.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;

@Entity
public class OneToOneTestEntity {

	@Id
	private Long id;

	@OneToOne
	private RelatedEntity oneRelatedValue;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public RelatedEntity getOneRelatedValue() {
		return oneRelatedValue;
	}

	public void setOneRelatedValue(RelatedEntity oneRelatedValue) {
		this.oneRelatedValue = oneRelatedValue;
	}
}
