package io.crnk.core.engine.internal.dispatcher.path;

import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.engine.registry.RegistryEntry;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * Represent a JSON API path sent to the server. Each resource or field defined in the path is represented by one
 * derived class of JsonPath object.
 * <p>
 * It is represented in a form of a doubly-linked list which must start with one or more ResourcePath instances and can
 * end with either FieldPath or LinksPath instance.
 */
public abstract class JsonPath {

	public static final String ID_SEPARATOR = ",";
	public static final String ID_SEPARATOR_PATTERN = ",|%2C";

	private RegistryEntry rootEntry;

	private List<Serializable> ids;

	public JsonPath(RegistryEntry rootEntry, List<Serializable> ids) {
		this.rootEntry = rootEntry;
		this.ids = ids;
	}

	public Collection<Serializable> getIds() {
		return ids;
	}

	public abstract boolean isCollection();

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		return o.toString().equals(toString());
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(rootEntry.getResourceInformation().getResourcePath());
		if (ids != null) {
			builder.append("/");
			builder.append(ids);
		}
		return builder.toString();
	}

	public Serializable getId() {
		PreconditionUtil.verify(ids.size() == 1, "single id expected, got %s", ids);
		return ids.get(0);
	}

	public RegistryEntry getRootEntry() {
		return rootEntry;
	}

}
