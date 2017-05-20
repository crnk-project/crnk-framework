package io.crnk.legacy.registry;

import io.crnk.core.engine.registry.ResourceEntry;
import io.crnk.core.engine.registry.ResponseRelationshipEntry;
import io.crnk.core.module.discovery.ResourceLookup;

import java.util.List;

/**
 * Using class of this type it's possible to build instances of document
 * entries, which can be used by other parts of the library.
 */
public interface RepositoryEntryBuilder {

	ResourceEntry buildResourceRepository(ResourceLookup lookup, Class<?> resourceClass);

	List<ResponseRelationshipEntry> buildRelationshipRepositories(ResourceLookup lookup, Class<?> resourceClass);
}
