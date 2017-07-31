package io.crnk.core.engine.internal.information.repository;

import io.crnk.core.engine.information.repository.RepositoryAction;
import io.crnk.core.engine.information.repository.ResourceRepositoryInformation;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.utils.Optional;

import java.util.HashMap;
import java.util.Map;

public class ResourceRepositoryInformationImpl implements ResourceRepositoryInformation {

	private Optional<ResourceInformation> resourceInformation;
	private String resourceType;
	private String path;
	private Map<String, RepositoryAction> actions;

	@Deprecated
	public ResourceRepositoryInformationImpl(String path, ResourceInformation resourceInformation) {
		this(path, resourceInformation, new HashMap<String, RepositoryAction>());
	}

	@Deprecated
	public ResourceRepositoryInformationImpl(String path,
											 ResourceInformation resourceInformation, Map<String, RepositoryAction> actions) {
		this(path, resourceInformation.getResourceType(), actions);
		this.resourceInformation = Optional.of(resourceInformation);
	}

	public ResourceRepositoryInformationImpl(String path, String resourceType, Map<String, RepositoryAction> actions) {
		this.path = path;
		this.actions = actions;
		this.resourceInformation = Optional.empty();
		this.resourceType = resourceType;
	}

	@Override
	public Optional<ResourceInformation> getResourceInformation() {
		return resourceInformation;
	}

	@Override
	public String getResourceType() {
		return resourceType;
	}

	@Override
	public String getPath() {
		return path;
	}

	@Override
	public Map<String, RepositoryAction> getActions() {
		return actions;
	}
}