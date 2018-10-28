package io.crnk.data.facet.provider;

import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.parser.TypeParser;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.repository.ResourceRepositoryV2;

public interface FacetProviderContext {

	ResourceRepositoryV2 getRepository(String resourceType);

	ResourceInformation getResourceInformation(String resourceType);

	TypeParser getTypeParser();

	RegistryEntry getEntry(String resourceType);
}
