package io.crnk.core.engine.information.repository;

/**
 * Holds information about the type of a relationship repository.
 */
public interface RelationshipRepositoryInformation {

	String getSourceResourceType();

	String getTargetResourceType();
}
