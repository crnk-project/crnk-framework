package io.crnk.core.engine.information.repository;

import io.crnk.core.engine.information.resource.ResourceInformation;

import java.util.Map;

/**
 * Holds information about the type of a resource repository.
 */
public interface ResourceRepositoryInformation extends RepositoryInformation {

	/**
	 * @return information about the resources hold in this resource
	 */
	ResourceInformation getResourceInformation();

	/**
	 * @return path from which the repository is accessible
	 */
	String getPath();


	Map<String, RepositoryAction> getActions();
}
