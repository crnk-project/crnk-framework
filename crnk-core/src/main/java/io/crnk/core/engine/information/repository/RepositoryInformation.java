package io.crnk.core.engine.information.repository;

import io.crnk.core.engine.information.resource.ResourceInformation;

/**
 * Holds information about the type of a repository.
 */
public interface RepositoryInformation {

	/**
	 * @return information about the resources hold in this resource
	 */
	ResourceInformation getResourceInformation();
}
