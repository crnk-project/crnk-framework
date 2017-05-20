package io.crnk.core.engine.internal.dispatcher.path;

import java.util.Objects;

/**
 * Represent a JSON API path sent to the server. Each resource or field defined in the path is represented by one
 * derived class of JsonPath object.
 * <p>
 * It is represented in a form of a doubly-linked list which must start with one or more ResourcePath instances and can
 * end with either FieldPath or LinksPath instance.
 */
public abstract class JsonPath {

	/**
	 * Name of a resource or a filed
	 */
	final String elementName;

	/**
	 * Unique identifier of a field
	 */
	PathIds ids;

	/**
	 * Entry closer to path's beginning
	 */
	JsonPath parentResource;

	/**
	 * Entry closer to path's end
	 */
	private JsonPath childResource;

	public JsonPath(String elementName) {
		this(elementName, null);
	}

	public JsonPath(String elementName, PathIds pathIds) {
		this.elementName = elementName;
		this.ids = pathIds;
	}

	/**
	 * Returns true if a JsonPath concerns a collection.
	 * It can happen if there's no or more than one id provided.
	 *
	 * @return true if a path concern a collection
	 */
	public abstract boolean isCollection();

	/**
	 * Returns name of a resource the last resource in requested path.
	 * There can be paths that concern relations. In this case a elementName from parent JsonPath should be retrieved.
	 *
	 * @return nam of the lase resource
	 */
	public abstract String getResourceName();

	/**
	 * Returns name of the current element. It can be either resource type or resource's field.
	 *
	 * @return name of the element
	 */
	public String getElementName() {
		return elementName;
	}

	public PathIds getIds() {
		return ids;
	}

	public void setIds(PathIds ids) {
		this.ids = ids;
	}

	public JsonPath getParentResource() {
		return parentResource;
	}

	public void setParentResource(JsonPath parentResource) {
		this.parentResource = parentResource;
	}

	public JsonPath getChildResource() {
		return childResource;
	}

	public void setChildResource(JsonPath childResource) {
		this.childResource = childResource;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		JsonPath jsonPath = (JsonPath) o;
		return Objects.equals(elementName, jsonPath.elementName) &&
				Objects.equals(ids, jsonPath.ids) &&
				Objects.equals(parentResource, jsonPath.parentResource);
	}

	@Override
	public int hashCode() {
		return Objects.hash(elementName, ids, parentResource);
	}
}
