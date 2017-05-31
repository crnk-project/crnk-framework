package io.crnk.legacy.registry;

import io.crnk.legacy.internal.DirectResponseRelationshipEntry;
import io.crnk.legacy.internal.DirectResponseResourceEntry;
import io.crnk.core.engine.registry.ResourceEntry;
import io.crnk.core.engine.registry.ResponseRelationshipEntry;
import io.crnk.core.exception.RepositoryInstanceNotFoundException;
import io.crnk.core.module.discovery.ResourceLookup;
import io.crnk.core.repository.RelationshipRepositoryV2;
import io.crnk.core.repository.ResourceRepositoryV2;
import io.crnk.legacy.locator.JsonServiceLocator;
import io.crnk.legacy.repository.RelationshipRepository;
import io.crnk.legacy.repository.ResourceRepository;
import net.jodah.typetools.TypeResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Repository entries builder for classes implementing document interfaces.
 */
public class DirectRepositoryEntryBuilder implements RepositoryEntryBuilder {
	private static final Logger LOGGER = LoggerFactory.getLogger(DirectRepositoryEntryBuilder.class);

	private final JsonServiceLocator jsonServiceLocator;

	public DirectRepositoryEntryBuilder(JsonServiceLocator jsonServiceLocator) {
		this.jsonServiceLocator = jsonServiceLocator;
	}

	@Override
	public ResourceEntry buildResourceRepository(ResourceLookup lookup, Class<?> resourceClass) {
		Class<?> repoClass = getRepoClassType(lookup.getResourceRepositoryClasses(), resourceClass);

		if (repoClass == null) {
			return null;
		}
		@SuppressWarnings("unchecked")
		DirectResponseResourceEntry directResourceEntry = new DirectResponseResourceEntry(new RepositoryInstanceBuilder(jsonServiceLocator, repoClass));
		return directResourceEntry;
	}

	private Class<?> getRepoClassType(Set<Class<?>> repositoryClasses, Class<?> resourceClass) {
		for (Class<?> repoClass : repositoryClasses) {
			if (ResourceRepository.class.isAssignableFrom(repoClass)) {
				Class<?>[] typeArgs = TypeResolver.resolveRawArguments(ResourceRepository.class, repoClass);
				if (typeArgs[0] == resourceClass) {
					return repoClass;
				}
			}
			if (ResourceRepositoryV2.class.isAssignableFrom(repoClass)) {
				Class<?>[] typeArgs = TypeResolver.resolveRawArguments(ResourceRepositoryV2.class, repoClass);
				if (typeArgs[0] == resourceClass) {
					return repoClass;
				}
			}

		}
		return null;
	}

	@Override
	public List<ResponseRelationshipEntry> buildRelationshipRepositories(ResourceLookup lookup, Class<?> resourceClass) {
		Set<Class<?>> relationshipRepositoryClasses = lookup.getResourceRepositoryClasses();

		Set<Class<?>> relationshipRepositories = findRelationshipRepositories(resourceClass, relationshipRepositoryClasses);

		List<ResponseRelationshipEntry> relationshipEntries = new LinkedList<>();
		for (Class<?> relationshipRepositoryClass : relationshipRepositories) {
			Object relationshipRepository = jsonServiceLocator.getInstance(relationshipRepositoryClass);
			if (relationshipRepository == null) {
				throw new RepositoryInstanceNotFoundException(relationshipRepositoryClass.getCanonicalName());
			}

			LOGGER.debug("Assigned {} RelationshipRepository  to {} resource class", relationshipRepositoryClass.getCanonicalName
					(), resourceClass.getCanonicalName());

			@SuppressWarnings("unchecked")
			DirectResponseRelationshipEntry relationshipEntry = new DirectResponseRelationshipEntry(new RepositoryInstanceBuilder<>(jsonServiceLocator, (Class<RelationshipRepository>) relationshipRepositoryClass));
			relationshipEntries.add(relationshipEntry);
		}
		return relationshipEntries;
	}

	private Set<Class<?>> findRelationshipRepositories(Class resourceClass, Set<Class<?>> relationshipRepositoryClasses) {
		Set<Class<?>> relationshipRepositories = new HashSet<>();
		for (Class<?> repoClass : relationshipRepositoryClasses) {
			if (RelationshipRepository.class.isAssignableFrom(repoClass)) {
				Class<?>[] typeArgs = TypeResolver.resolveRawArguments(RelationshipRepository.class, repoClass);
				if (typeArgs[0] == resourceClass) {
					relationshipRepositories.add(repoClass);
				}
			}
			if (RelationshipRepositoryV2.class.isAssignableFrom(repoClass)) {
				Class<?>[] typeArgs = TypeResolver.resolveRawArguments(RelationshipRepositoryV2.class, repoClass);
				if (typeArgs[0] == resourceClass) {
					relationshipRepositories.add(repoClass);
				}
			}
		}

		return relationshipRepositories;
	}
}
