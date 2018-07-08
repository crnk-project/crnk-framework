package io.crnk.core.engine.internal.dispatcher.path;

import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.registry.RegistryEntry;

import java.io.Serializable;
import java.util.List;

/**
 * Represents a part of a path which relate a field of a resource e.g. for /resource/1/field the first element will be
 * an object of ResourcePath type and the second will be of FieldPath type.
 * <p>
 * FieldPath can refer only to relationship fields.
 */
public class FieldPath extends JsonPath {

	private final ResourceField field;

	public FieldPath(RegistryEntry rootEntry, List<Serializable> ids, ResourceField field) {
		super(rootEntry, ids);
		this.field = field;
	}

	@Override
	public boolean isCollection() {
		return getIds() == null || getIds().size() > 1;
	}

	public ResourceField getField() {
		return field;
	}

	@Override
	public String toString() {
		return super.toString() + "/" + field;
	}

}
