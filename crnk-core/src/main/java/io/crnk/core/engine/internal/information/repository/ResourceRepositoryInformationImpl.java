package io.crnk.core.engine.internal.information.repository;

import io.crnk.core.engine.information.repository.RepositoryAction;
import io.crnk.core.engine.information.repository.ResourceRepositoryInformation;
import io.crnk.core.engine.information.resource.ResourceInformation;

import java.util.HashMap;
import java.util.Map;

public class ResourceRepositoryInformationImpl extends RepositoryInformationImpl implements ResourceRepositoryInformation {

	private String path;
	private Map<String, RepositoryAction> actions;

	public ResourceRepositoryInformationImpl(String path, ResourceInformation resourceInformation) {
		this(path, resourceInformation, new HashMap<String, RepositoryAction>());
	}

	public ResourceRepositoryInformationImpl(String path,
											 ResourceInformation resourceInformation, Map<String, RepositoryAction> actions) {
		super(resourceInformation);
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