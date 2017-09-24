package io.crnk.core.engine.filter;

import io.crnk.core.engine.document.ResourceIdentifier;
import io.crnk.core.engine.information.resource.ResourceField;

import java.util.List;

public class ResourceModificationFilterBase implements ResourceModificationFilter {

	@Override
	public <T> T modifyAttribute(Object resource, ResourceField field, String fieldName, T value) {
		return value;
	}

	@Override
	public ResourceIdentifier modifyOneRelationship(Object resource, ResourceField field, ResourceIdentifier id) {
		return id;
	}

	@Override
	public List<ResourceIdentifier> modifyManyRelationship(Object resource, ResourceField field, ResourceRelationshipModificationType modificationType, List<ResourceIdentifier> ids) {
		return ids;
	}
}
