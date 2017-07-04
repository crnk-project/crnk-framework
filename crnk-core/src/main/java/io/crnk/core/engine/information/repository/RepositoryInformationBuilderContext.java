package io.crnk.core.engine.information.repository;

import io.crnk.core.engine.information.InformationBuilder;
import io.crnk.core.engine.information.resource.ResourceInformationBuilder;
import io.crnk.core.engine.parser.TypeParser;

public interface RepositoryInformationBuilderContext {

	ResourceInformationBuilder getResourceInformationBuilder();

	TypeParser getTypeParser();

	InformationBuilder builder();
}
