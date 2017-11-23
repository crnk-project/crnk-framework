package io.crnk.core.engine.document;

import java.util.Objects;

public class ResourceIdentifier implements Comparable<ResourceIdentifier> {

	protected String id;

	protected String type;

	public ResourceIdentifier() {
	}

	public ResourceIdentifier(String id, String type) {
		this.id = id;
		this.type = type;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, type);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || obj.getClass() != this.getClass()) {
			return false;
		}
		ResourceIdentifier other = (ResourceIdentifier) obj;
		return Objects.equals(id, other.id) && Objects.equals(type, other.type);
	}

	@Override
	public int compareTo(ResourceIdentifier o) {
		int d = type.compareTo(o.type);
		if (d != 0) {
			return d;
		}
		return id.compareTo(o.id);
	}

	@Override
	public String toString() {
		return "ResourceIdentifier{" +
				"id='" + id + '\'' +
				", type='" + type + '\'' +
				'}';
	}
}