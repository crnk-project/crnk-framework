package io.crnk.core.engine.information.repository;

import java.util.Optional;

/**
 * Holds information about the type of a relationship repository.
 */
public interface RelationshipRepositoryInformation extends RepositoryInformation {

	/**
	 * @return resource class on source side. Used if no resource repository is available to
	 * compute ResourceInformation with ResourceInformationProvider. Can be null otherwise
	 */
	Optional<Class> getSourceResourceClass();

	String getSourceResourceType();

	String getTargetResourceType();
}
