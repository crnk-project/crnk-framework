package io.crnk.core.engine.internal.dispatcher.path;

/**
 * Represents a part of a path which relate a field of a resource e.g. for /resource/1 will be represented as
 * an object of ResourcePath type.
 */
public class ResourcePath extends JsonPath {

	public ResourcePath(String elementName) {
		super(elementName);
	}

	public ResourcePath(String elementName, PathIds pathIds) {
		super(elementName, pathIds);
	}

	@Override
	public boolean isCollection() {
		return ids == null || ids.getIds().size() > 1;
	}

	@Override
	public String getResourceType() {
		return elementName;
	}
}
