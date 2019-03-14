package io.crnk.core.exception;

import io.crnk.core.engine.information.resource.ResourceField;

/**
 * An exception which is thrown when a relationship repository for a classes is not found in specific package
 */
public class RelationshipRepositoryNotFoundException extends InternalServerErrorException {// NOSONAR ignore deep class hierarchy
	private static final String LEGACY_MESSAGE = "Couldn't find a relationship repository for resourceType=%s and field=%s";
	private static final String MESSAGE = "Couldn't find a relationship repository for resourceType=%s and field=%s, hasIdField=%s, repositoryBehavior=%s";

	/**
	 * @deprecated use other constructor
	 */
	@Deprecated
	public RelationshipRepositoryNotFoundException(String sourceResourceType, String targetResourceType) {
		super(String.format(LEGACY_MESSAGE, sourceResourceType, targetResourceType));
	}

	public RelationshipRepositoryNotFoundException(ResourceField field) {
		super(String.format(MESSAGE, field.getResourceInformation().getResourceType(), field.getUnderlyingName(), field.hasIdField(), field.getRelationshipRepositoryBehavior()));
	}
}
