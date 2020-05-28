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
	 * @deprecated use getResource
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

	/**
	 * @return true if the repository is available from the endpoint. Enabled by default, but not desired in all situations. For
	 * example in a micro-service architecture, one may want to register a remote repository of another micro service to perform
	 * relationship lookups.
	 */
	boolean isExposed();
}
