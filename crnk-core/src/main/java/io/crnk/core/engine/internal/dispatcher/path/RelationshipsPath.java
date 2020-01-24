package io.crnk.core.engine.internal.dispatcher.path;

import java.io.Serializable;
import java.util.List;

import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.registry.RegistryEntry;

/**
 * Represents a part of a path which relate a field of a resource e.g. for /resource/1/relationships/field the first
 * element will be an object of {@link ResourcePath} class and the second will be of {@link RelationshipsPath} type.
 * <p>
 * {@link RelationshipsPath} can refer only to relationship fields.
 */
public class RelationshipsPath extends JsonPath {

	private ResourceField relationship;

	public RelationshipsPath(RegistryEntry rootEntry, List<Serializable> ids, ResourceField relationship) {
		super(rootEntry, ids);
		this.relationship = relationship;
	}

	@Override
	public boolean isCollection() {
		return getIds() == null || getIds().size() > 1;
	}

	public ResourceField getRelationship() {
		return relationship;
	}

	@Override
	public String toString() {
		return super.toString() + "/relationships/" + relationship.getJsonName();
	}

	@Override
	public String toGroupPath() {
		String groupedUrl = super.toGroupPath();
		return groupedUrl + "/relationships/" + relationship.getJsonName();
	}
}
