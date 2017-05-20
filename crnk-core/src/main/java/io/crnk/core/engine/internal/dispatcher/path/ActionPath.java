package io.crnk.core.engine.internal.dispatcher.path;

/**
 * Represents a part of a path which relate a field of a resource e.g. for /resource/1/field the first element will be
 * an object of ResourcePath type and the second will be of FieldPath type.
 * <p>
 * FieldPath can refer only to relationship fields.
 */
public class ActionPath extends JsonPath {

	public ActionPath(String elementName) {
		super(elementName);
	}

	@Override
	public boolean isCollection() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getResourceName() {
		throw new UnsupportedOperationException();
	}
}
