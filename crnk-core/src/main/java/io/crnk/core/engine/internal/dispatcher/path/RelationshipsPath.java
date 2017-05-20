package io.crnk.core.engine.internal.dispatcher.path;

/**
 * Represents a part of a path which relate a field of a resource e.g. for /resource/1/relationships/field the first
 * element will be an object of {@link ResourcePath} class and the second will be of {@link RelationshipsPath} type.
 * <p>
 * {@link RelationshipsPath} can refer only to relationship fields.
 */
public class RelationshipsPath extends FieldPath {

	public RelationshipsPath(String elementName) {
		super(elementName);
	}
}
