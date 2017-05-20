package io.crnk.core.engine.internal.information.repository;

import io.crnk.core.engine.information.repository.RepositoryAction;
import io.crnk.core.engine.information.repository.ResourceRepositoryInformation;
import io.crnk.core.engine.information.resource.ResourceInformation;

import java.util.HashMap;
import java.util.Map;

public class ResourceRepositoryInformationImpl extends RepositoryInformationImpl implements ResourceRepositoryInformation {

	private String path;
	private Map<String, RepositoryAction> actions;

	public ResourceRepositoryInformationImpl(Class<?> repositoryClass, String path, ResourceInformation resourceInformation) {
		this(repositoryClass, path, resourceInformation, new HashMap<String, RepositoryAction>());
	}

	public ResourceRepositoryInformationImpl(Class<?> repositoryClass, String path,
											 ResourceInformation resourceInformation, Map<String, RepositoryAction> actions) {
		super(repositoryClass, resourceInformation);
		this.path = path;
		this.actions = actions;
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