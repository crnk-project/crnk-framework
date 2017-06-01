package io.crnk.core.module.internal;

import io.crnk.core.engine.information.repository.RepositoryInformationBuilderContext;
import io.crnk.core.engine.information.resource.ResourceInformationBuilder;
import io.crnk.core.engine.parser.TypeParser;
import io.crnk.core.module.ModuleRegistry;

public class DefaultRepositoryInformationBuilderContext implements RepositoryInformationBuilderContext {

	private ModuleRegistry moduleRegistry;

	public DefaultRepositoryInformationBuilderContext(ModuleRegistry moduleRegistry) {
		this.moduleRegistry = moduleRegistry;
	}

	@Override
	public ResourceInformationBuilder getResourceInformationBuilder() {
		return moduleRegistry.getResourceInformationBuilder();
	}

	@Override
	public TypeParser getTypeParser() {
		return moduleRegistry.getTypeParser();
	}
}
