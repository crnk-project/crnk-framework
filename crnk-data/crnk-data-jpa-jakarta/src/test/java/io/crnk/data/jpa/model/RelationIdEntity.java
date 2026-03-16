package io.crnk.data.jpa.model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import io.crnk.core.resource.annotations.JsonApiRelationId;

@Entity
public class RelationIdEntity extends TestMappedSuperclass {

	@Id
	private Long id;

	@JsonApiRelationId
	private Long oneRelatedValueId;

	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
	@JoinColumn
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

	public Long getOneRelatedValueId() {
		return oneRelatedValueId;
	}

	public void setOneRelatedValueId(Long oneRelatedValueId) {
		this.oneRelatedValueId = oneRelatedValueId;
	}
}
