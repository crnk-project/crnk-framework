package io.crnk.core.engine.information.repository;

import io.crnk.core.engine.information.resource.ResourceInformation;

import java.util.Map;
import io.crnk.core.utils.Optional;

/**
 * Holds information about the type of a resource repository.
 */
public interface ResourceRepositoryInformation extends RepositoryInformation {

	/**
	 * @return information about the resources hold in this resource
	 * @deprecated use getReosurce
	 */
	@Deprecated
	Optional<ResourceInformation> getResourceInformation();

	/**
	 * @return information about the resources hold in this resource
	 */
	ResourceInformation getResource();

	/**
	 * @deprecated use getResource
	 */
	@Deprecated
	String getResourceType();

	/**
	 * @return path from which the repository is accessible
	 */
	String getPath();


	Map<String, RepositoryAction> getActions();
}
