package io.crnk.core.module.internal;

import io.crnk.core.engine.information.InformationBuilder;
import io.crnk.core.engine.information.repository.RepositoryInformationProviderContext;
import io.crnk.core.engine.information.resource.ResourceInformationProvider;
import io.crnk.core.engine.parser.TypeParser;
import io.crnk.core.module.ModuleRegistry;

public class DefaultRepositoryInformationProviderContext implements RepositoryInformationProviderContext {

	private ModuleRegistry moduleRegistry;

	public DefaultRepositoryInformationProviderContext(ModuleRegistry moduleRegistry) {
		this.moduleRegistry = moduleRegistry;
	}

	@Override
	public ResourceInformationProvider getResourceInformationBuilder() {
		return moduleRegistry.getResourceInformationBuilder();
	}

	@Override
	public TypeParser getTypeParser() {
		return moduleRegistry.getTypeParser();
	}

	@Override
	public InformationBuilder builder() {
		return moduleRegistry.getInformationBuilder();
	}
}
