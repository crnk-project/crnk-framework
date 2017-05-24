package io.crnk.core.engine.document;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ResourceIdentifier implements Comparable<ResourceIdentifier> {

	private String id;

	private String type;

	public ResourceIdentifier() {
	}

	public ResourceIdentifier(String id, String type) {
		this.id = id;
		this.type = type;
	}

	public static Object fromData(Object data) {
		if (data == null) {
			return null;
		}
		if (data instanceof Iterable) {
			List<ResourceIdentifier> result = new ArrayList<>();
			for (ResourceIdentifier id : (Iterable<ResourceIdentifier>) data) {
				result.add(id.duplicate());
			}
			return result;
		} else {
			ResourceIdentifier id = (ResourceIdentifier) data;
			return id.duplicate();
		}
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

	public ResourceIdentifier duplicate() {
		return new ResourceIdentifier(id, type);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, type);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || obj.getClass() != this.getClass())
			return false;
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