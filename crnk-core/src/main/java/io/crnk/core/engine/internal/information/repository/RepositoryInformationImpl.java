package io.crnk.core.engine.internal.information.repository;

import io.crnk.core.engine.information.repository.RepositoryInformation;
import io.crnk.core.engine.information.resource.ResourceInformation;

public abstract class RepositoryInformationImpl implements RepositoryInformation {

	private ResourceInformation resourceInformation;


	public RepositoryInformationImpl(ResourceInformation resourceInformation) {
		super();
		this.resourceInformation = resourceInformation;
	}


	@Override
	public ResourceInformation getResourceInformation() {
		return resourceInformation;
	}
}