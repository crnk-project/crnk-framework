package io.crnk.test.mock.models.nested;

import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiRelationId;

import java.io.Serializable;

public class NestedId implements Serializable {

	@JsonApiId
	private String id;

	@JsonApiRelationId
	private String parentId;

	public NestedId() {

	}

	public NestedId(String parentId, String id) {
		this.parentId = parentId;
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public static NestedId parse(String idString) {
		String[] elements = idString.split("\\-");
		NestedId id = new NestedId();
		id.parentId = elements[0];
		id.id = elements[1];
		return id;
	}

	public int hashCode() {
		return toString().hashCode();
	}

	public boolean equals(Object object) {
		return object instanceof NestedId && object.toString().equals(toString());
	}

	public String toString() {
		return parentId + "-" + id;
	}
}
