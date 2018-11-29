package io.crnk.test.mock.models.nested;

import java.io.Serializable;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiRelationId;

@JsonSerialize(using = ToStringSerializer.class)
public class NestedId implements Serializable {

	@JsonApiId
	private String id;

	@JsonApiRelationId
	private String parentId;

	public NestedId() {
	}

	public NestedId(String idString) {
		String[] elements = idString.split("\\-");
		parentId = elements[0];
		id = elements[1];
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
