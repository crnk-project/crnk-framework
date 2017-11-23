package io.crnk.core.engine.filter;

import io.crnk.core.engine.document.ResourceIdentifier;
import io.crnk.core.engine.information.resource.ResourceField;

import java.util.List;

/**
 * Allows to intercept and filter resource modifications.
 * <p>
 * In incurbation and to change in future releases.
 */
public interface ResourceModificationFilter {

	<T> T modifyAttribute(Object resource, ResourceField field, String fieldName, T value);

	ResourceIdentifier modifyOneRelationship(Object resource, ResourceField field, ResourceIdentifier id);

	List<ResourceIdentifier> modifyManyRelationship(Object resource, ResourceField field, ResourceRelationshipModificationType modificationType, List<ResourceIdentifier> ids);

}
