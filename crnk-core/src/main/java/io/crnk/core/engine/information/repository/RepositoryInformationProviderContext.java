package io.crnk.core.engine.information.repository;

import io.crnk.core.engine.information.InformationBuilder;
import io.crnk.core.engine.information.resource.ResourceInformationProvider;
import io.crnk.core.engine.parser.TypeParser;

public interface RepositoryInformationProviderContext {

	ResourceInformationProvider getResourceInformationBuilder();

	TypeParser getTypeParser();

	InformationBuilder builder();
}
