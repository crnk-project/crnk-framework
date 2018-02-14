package io.crnk.core.engine.information.repository;

import io.crnk.core.engine.information.resource.ResourceInformation;

import java.util.Map;
import java.util.Optional;

/**
 * Holds information about the type of a resource repository.
 */
public interface ResourceRepositoryInformation extends RepositoryInformation {

	/**
	 * @return information about the resources hold in this resource
	 */
	Optional<ResourceInformation> getResourceInformation();

	String getResourceType();

	/**
	 * @return path from which the repository is accessible
	 */
	String getPath();


	Map<String, RepositoryAction> getActions();
}
