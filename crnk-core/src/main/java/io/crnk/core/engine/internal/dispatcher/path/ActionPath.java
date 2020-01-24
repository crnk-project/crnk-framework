package io.crnk.core.engine.internal.dispatcher.path;

import java.io.Serializable;
import java.util.List;

import io.crnk.core.engine.registry.RegistryEntry;

/**
 * Represents a part of a path which relate a field of a resource e.g. for /resource/1/field the first element will be
 * an object of ResourcePath type and the second will be of FieldPath type.
 * <p>
 * FieldPath can refer only to relationship fields.
 */
public class ActionPath extends JsonPath {

	private final String actionName;

	public ActionPath(RegistryEntry rootEntry, List<Serializable> ids, String actionName) {
		super(rootEntry, ids);
		this.actionName = actionName;
	}

	public String getActionName() {
		return actionName;
	}

	@Override
	public boolean isCollection() {
		return getIds() == null || getIds().size() > 1;
	}

	@Override
	public String toString() {
		return super.toString() + "/" + actionName;
	}

	@Override
	public String toGroupPath() {
		String groupedUrl = super.toGroupPath();
		return groupedUrl + "/" + actionName;
	}
}
