package io.crnk.core.engine.internal.dispatcher.path;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.engine.registry.RegistryEntry;

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

	private ResourceField parentField;

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

	/**
	 * Replaces resource identifiers with {id} to have nice urls for e.g. tracing.
	 */
	public String toGroupPath() {
		RegistryEntry rootEntry = getRootEntry();
		ResourceInformation resourceInformation = rootEntry.getResourceInformation();

		String resourcePath;
		if (parentField != null) {
			// TODO nested resource can be queried through two means, nested or direct flat (maybe). should be stored and considered somewhere here
			ResourceInformation parentType = parentField.getParentResourceInformation();
			resourcePath = parentType.getResourcePath() + "/{id}/" + parentField.getJsonName();
		}
		else {
			resourcePath = resourceInformation.getResourcePath();
		}

		if (getIds() != null) {
			resourcePath += "/{id}";
		}
		return resourcePath;
	}

	/**
	 * Used in case of nesting of resources.
	 */
	void addParentField(ResourceField parentField){
		this.parentField = parentField;
	}
}
