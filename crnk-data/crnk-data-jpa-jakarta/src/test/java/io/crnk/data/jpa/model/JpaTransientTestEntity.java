package io.crnk.data.jpa.model;


import io.crnk.core.resource.annotations.JsonApiRelation;
import io.crnk.core.resource.annotations.LookupIncludeBehavior;
import io.crnk.core.resource.annotations.SerializeType;
import io.crnk.test.mock.models.Task;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

/**
 * Holds transient link to resource in a transient field
 */
// tag::docs[]
@Entity
public class JpaTransientTestEntity extends TestMappedSuperclass {

	@Id
	private Long id;

	@Transient
	@JsonApiRelation(serialize = SerializeType.LAZY, lookUp = LookupIncludeBehavior.NONE)
	private Task task;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Task getTask() {
		return task;
	}

	public void setTask(Task task) {
		this.task = task;
	}
}
// end::docs[]