package io.crnk.core.engine.internal.information.repository;

import java.util.Map;
import java.util.Optional;

import io.crnk.core.engine.information.repository.RepositoryAction;
import io.crnk.core.engine.information.repository.RepositoryMethodAccess;
import io.crnk.core.engine.information.repository.ResourceRepositoryInformation;
import io.crnk.core.engine.information.resource.ResourceInformation;

public class ResourceRepositoryInformationImpl implements ResourceRepositoryInformation {

	private ResourceInformation resourceInformation;


	private String path;

	private Map<String, RepositoryAction> actions;

	private RepositoryMethodAccess access;

	private boolean exposed;

	public ResourceRepositoryInformationImpl(String path,
			ResourceInformation resourceInformation, Map<String, RepositoryAction> actions,
			RepositoryMethodAccess access, boolean exposed) {
		this.resourceInformation = resourceInformation;
		this.path = path;
		this.actions = actions;
		this.access = access;
		this.exposed = exposed;
	}

	@Override
	public Optional<ResourceInformation> getResourceInformation() {
		return Optional.of(getResource());
	}

	@Override
	public ResourceInformation getResource() {
		return resourceInformation;
	}

	@Override
	public String getResourceType() {
		return resourceInformation.getResourceType();
	}

	@Override
	public String getPath() {
		return path;
	}

	@Override
	public Map<String, RepositoryAction> getActions() {
		return actions;
	}

	@Override
	public boolean isExposed() {
		return exposed;
	}

	@Override
	public RepositoryMethodAccess getAccess() {
		return access;
	}
}