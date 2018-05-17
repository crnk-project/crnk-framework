package io.crnk.core.engine.internal.dispatcher.path;

import java.util.Objects;

import io.crnk.core.engine.internal.utils.CompareUtils;

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
	public abstract String getResourcePath();

	/**
	 * Returns name of the current element. It can be either resource type or resource's field.
	 *
	 * @return name of the element
	 */
	// TODO separte resource type from resource field
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

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		JsonPath jsonPath = (JsonPath) o;
		return CompareUtils.isEquals(elementName, jsonPath.elementName) &&
				CompareUtils.isEquals(ids, jsonPath.ids) &&
				CompareUtils.isEquals(parentResource, jsonPath.parentResource);
	}

	@Override
	public int hashCode() {
		return Objects.hash(elementName, ids, parentResource);
	}

	@Override
	public String toString(){
		StringBuilder builder = new StringBuilder();
		if(parentResource != null){
			builder.append(parentResource.toString());
			builder.append("/");
		}
		if(elementName != null){
			builder.append(elementName);
			builder.append("/");
		}
		if(ids != null){
			builder.append(ids);
		}
		return builder.toString();
	}
}
