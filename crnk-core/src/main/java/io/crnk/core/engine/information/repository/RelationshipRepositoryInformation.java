package io.crnk.core.engine.information.repository;

import io.crnk.core.engine.information.resource.ResourceInformation;

/**
 * Holds information about the type of a relationship repository.
 */
public interface RelationshipRepositoryInformation extends RepositoryInformation {

	/**
	 * @return information about the source of the relationship.
	 */
	ResourceInformation getSourceResourceInformation();

}
