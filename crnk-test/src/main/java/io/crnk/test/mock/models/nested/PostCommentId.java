package io.crnk.test.mock.models.nested;

import java.io.Serializable;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiRelationId;

// tag::docs[]
@JsonSerialize(using = ToStringSerializer.class)
public class PostCommentId implements Serializable {

	@JsonApiId
	private String id;

	@JsonApiRelationId
	private String postId;

	public PostCommentId() {
	}

	public PostCommentId(String idString) {
		String[] elements = idString.split("\\-");
		postId = elements[0];
		id = elements[1];
	}

	public PostCommentId(String postId, String id) {
		this.postId = postId;
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPostId() {
		return postId;
	}

	public void setPostId(String postId) {
		this.postId = postId;
	}

	public int hashCode() {
		return toString().hashCode();
	}

	public boolean equals(Object object) {
		return object instanceof PostCommentId && object.toString().equals(toString());
	}

	public String toString() {
		return postId + "-" + id;
	}
}
// end::docs[]