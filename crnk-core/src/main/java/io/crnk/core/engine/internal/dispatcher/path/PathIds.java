package io.crnk.core.engine.internal.dispatcher.path;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Represent an id or ids passed in the path from a client.
 */
public class PathIds {
	public static final String ID_SEPARATOR = ",";
	public static final String ID_SEPARATOR_PATTERN = ",|%2C";

	private final List<String> ids = new LinkedList<>();

	public PathIds(@SuppressWarnings("SameParameterValue") String id) {
		ids.add(id);
	}

	public PathIds(Collection<String> id) {
		ids.addAll(id);
	}

	public List<String> getIds() {
		return ids;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		PathIds pathIds = (PathIds) o;

		return ids.equals(pathIds.ids);

	}

	@Override
	public int hashCode() {
		return ids.hashCode();
	}

	@Override
	public String toString() {
		return ids.toString();
	}
}
