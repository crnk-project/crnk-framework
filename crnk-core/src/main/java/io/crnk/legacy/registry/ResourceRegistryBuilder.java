package io.crnk.legacy.registry;

import io.crnk.core.engine.information.repository.RepositoryMethodAccess;
import io.crnk.core.engine.information.repository.ResourceRepositoryInformation;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.information.resource.ResourceInformationBuilder;
import io.crnk.core.engine.internal.information.repository.ResourceRepositoryInformationImpl;
import io.crnk.core.engine.internal.registry.ResourceRegistryImpl;
import io.crnk.core.engine.registry.*;
import io.crnk.core.engine.url.ServiceUrlProvider;
import io.crnk.core.module.ModuleRegistry;
import io.crnk.core.module.discovery.DefaultResourceLookup;
import io.crnk.core.module.discovery.ResourceLookup;
import io.crnk.legacy.locator.JsonServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Builder responsible for building an instance of ResourceRegistry.
 *
 * @deprecated make use of {@link io.crnk.core.boot.CrnkBoot}
 */
@Deprecated
public class ResourceRegistryBuilder {
	private static final Logger LOGGER = LoggerFactory.getLogger(ResourceRegistryBuilder.class);

	private final ResourceInformationBuilder resourceInformationBuilder;
	private final RepositoryEntryBuilderFacade repositoryEntryBuilder;

	public ResourceRegistryBuilder(ModuleRegistry moduleRegistry, JsonServiceLocator jsonServiceLocator, ResourceInformationBuilder resourceInformationBuilder) {
		this.resourceInformationBuilder = resourceInformationBuilder;
		this.repositoryEntryBuilder = new RepositoryEntryBuilderFacade(moduleRegistry, jsonServiceLocator);

		DefaultResourceInformationBuilderContext context = new DefaultResourceInformationBuilderContext(resourceInformationBuilder, moduleRegistry.getTypeParser());
		resourceInformationBuilder.init(context);
	}

	/**
	 * Uses a {@link DefaultResourceLookup} to get all classes in provided
	 * package and finds all resources and repositories associated with found
	 * document.
	 *
	 * @param packageName        Package containing resources (models) and repositories.
	 * @param serviceUrlProvider Compute the document to this service
	 * @return an instance of ResourceRegistry
	 */
	public ResourceRegistry build(String packageName, ModuleRegistry moduleRegistry, ServiceUrlProvider serviceUrlProvider) {
		return build(new DefaultResourceLookup(packageName), moduleRegistry, serviceUrlProvider);
	}

	/**
	 * Uses a {@link ResourceLookup} to get all resources and repositories
	 * associated with found document.
	 *
	 * @param resourceLookup Lookup for getting all document classes.
	 * @param serviceUrl     URL to the service
	 * @return an instance of ResourceRegistry
	 */
	public ResourceRegistry build(ResourceLookup resourceLookup, ModuleRegistry moduleRegistry, ServiceUrlProvider serviceUrl) {
		Set<Class<?>> jsonApiResources = resourceLookup.getResourceClasses();

		Set<ResourceInformation> resourceInformationSet = new HashSet<>(jsonApiResources.size());


		for (Class<?> clazz : jsonApiResources) {
			resourceInformationSet.add(resourceInformationBuilder.build(clazz));
			LOGGER.trace("{} registered as a resource", clazz);
		}

		Set<RegistryEntry> registryEntries = new HashSet<>(resourceInformationSet.size());
		for (ResourceInformation resourceInformation : resourceInformationSet) {
			Class<?> resourceClass = resourceInformation.getResourceClass();

			ResourceEntry resourceEntry = repositoryEntryBuilder.buildResourceRepository(resourceLookup, resourceClass);
			LOGGER.trace("{} has a resource repository {}", resourceInformation.getResourceClass(), resourceEntry);
			List<ResponseRelationshipEntry> relationshipEntries = repositoryEntryBuilder.buildRelationshipRepositories(resourceLookup, resourceClass);
			LOGGER.trace("{} has relationship repositories {}", resourceInformation.getResourceClass(), relationshipEntries);

			RepositoryMethodAccess access = new RepositoryMethodAccess(true, true, true, true);
			ResourceRepositoryInformation repositoryInformation = new ResourceRepositoryInformationImpl(resourceInformation.getResourceType(), resourceInformation, access);

			RegistryEntry entry = new RegistryEntry(resourceInformation, repositoryInformation, resourceEntry, relationshipEntries);
			entry.initialize(moduleRegistry);
			registryEntries.add(entry);
		}

		ResourceRegistry resourceRegistry = new ResourceRegistryImpl(new DefaultResourceRegistryPart(), moduleRegistry);
		for (RegistryEntry registryEntry : registryEntries) {
			Class<?> resourceClass = registryEntry.getResourceInformation().getResourceClass();
			RegistryEntry registryEntryParent = findParent(resourceClass, registryEntries);
			registryEntry.setParentRegistryEntry(registryEntryParent);
			resourceRegistry.addEntry(resourceClass, registryEntry);
		}

		return resourceRegistry;
	}

	/**
	 * Finds the closest document in the class inheritance hierarchy. If no
	 * document parent is found, <i>null</i> is returned.
	 *
	 * @param resourceClass   information about the searched document
	 * @param registryEntries a set of available resources
	 * @return document's parent document
	 */
	private RegistryEntry findParent(Class<?> resourceClass, Set<RegistryEntry> registryEntries) {
		RegistryEntry foundRegistryEntry = null;
		Class<?> currentClass = resourceClass.getSuperclass();
		classHierarchy:
		// goto statement?! Replace this with a recursion
		while (currentClass != null && currentClass != Object.class) {
			for (RegistryEntry availableRegistryEntry : registryEntries) {
				if (availableRegistryEntry.getResourceInformation().getResourceClass().equals(currentClass)) {
					foundRegistryEntry = availableRegistryEntry;
					break classHierarchy;
				}
			}
			currentClass = currentClass.getSuperclass();
		}
		return foundRegistryEntry;
	}
}
