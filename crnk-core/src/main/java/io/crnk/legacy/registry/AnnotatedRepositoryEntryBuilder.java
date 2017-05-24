package io.crnk.legacy.registry;

import io.crnk.core.engine.internal.utils.Predicate1;
import io.crnk.core.engine.registry.ResourceEntry;
import io.crnk.core.engine.registry.ResponseRelationshipEntry;
import io.crnk.core.module.ModuleRegistry;
import io.crnk.core.module.discovery.ResourceLookup;
import io.crnk.legacy.locator.JsonServiceLocator;
import io.crnk.legacy.repository.annotations.JsonApiRelationshipRepository;
import io.crnk.legacy.repository.annotations.JsonApiResourceRepository;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Repository entries builder for classes annotated with document annotations.
 */
public class AnnotatedRepositoryEntryBuilder implements RepositoryEntryBuilder {

	private final JsonServiceLocator jsonServiceLocator;

	private ModuleRegistry moduleRegistry;

	public AnnotatedRepositoryEntryBuilder(ModuleRegistry moduleRegistry, JsonServiceLocator jsonServiceLocator) {
		this.moduleRegistry = moduleRegistry;
		this.jsonServiceLocator = jsonServiceLocator;
	}

	@Override
	public ResourceEntry buildResourceRepository(ResourceLookup lookup, final Class<?> resourceClass) {
		Predicate1<Class<?>> classPredicate = new Predicate1<Class<?>>() {
			@Override
			public boolean test(Class<?> clazz) {
				return resourceClass.equals(clazz.getAnnotation(JsonApiResourceRepository.class).value());
			}
		};

		List<Class<?>> repositoryClasses = findRepositoryClasses(lookup, classPredicate, JsonApiResourceRepository.class);
		if (repositoryClasses.isEmpty()) {
			return null;
		} else {
			return new AnnotatedResourceEntry(new RepositoryInstanceBuilder<>(jsonServiceLocator, repositoryClasses.get(0)));
		}
	}

	@Override
	public List<ResponseRelationshipEntry> buildRelationshipRepositories(ResourceLookup lookup, final Class<?> resourceClass) {
		Predicate1<Class<?>> classPredicate = new Predicate1<Class<?>>() {
			@Override
			public boolean test(Class<?> clazz) {
				JsonApiRelationshipRepository annotation = clazz.getAnnotation(JsonApiRelationshipRepository.class);
				return resourceClass.equals(annotation.source());
			}
		};

		List<Class<?>> repositoryClasses = findRepositoryClasses(lookup, classPredicate, JsonApiRelationshipRepository.class);
		List<ResponseRelationshipEntry> relationshipEntries = new ArrayList<>(repositoryClasses.size());
		for (Class<?> repositoryClass : repositoryClasses) {
			relationshipEntries.add(new AnnotatedRelationshipEntryBuilder(moduleRegistry, new RepositoryInstanceBuilder<>(jsonServiceLocator, repositoryClass)));
		}

		return relationshipEntries;
	}

	private List<Class<?>> findRepositoryClasses(ResourceLookup lookup, Predicate1<Class<?>> classPredicate, Class<? extends Annotation> annotation) {
		List<Class<?>> repositoryClasses = new LinkedList<>();

		for (Class<?> clazz : lookup.getResourceRepositoryClasses()) {
			if (clazz.isAnnotationPresent(annotation) && classPredicate.test(clazz)) {
				repositoryClasses.add(clazz);
			}
		}
		return repositoryClasses;
	}
}
