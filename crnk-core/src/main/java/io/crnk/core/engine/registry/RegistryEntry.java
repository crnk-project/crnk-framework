package io.crnk.core.engine.registry;

import io.crnk.core.engine.information.repository.ResourceRepositoryInformation;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.registry.DirectResponseRelationshipEntry;
import io.crnk.core.engine.internal.registry.DirectResponseResourceEntry;
import io.crnk.core.engine.internal.repository.RelationshipRepositoryAdapter;
import io.crnk.core.engine.internal.repository.ResourceRepositoryAdapter;
import io.crnk.core.exception.RelationshipRepositoryNotFoundException;
import io.crnk.core.module.ModuleRegistry;
import io.crnk.legacy.internal.RepositoryMethodParameterProvider;
import io.crnk.legacy.registry.AnnotatedRelationshipEntryBuilder;
import io.crnk.legacy.registry.AnnotatedResourceEntry;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * Holds information about a resource of type <i>T</i> and its repositories. It
 * includes the following information: - ResourceInformation instance with
 * information about the resource, - ResourceEntry instance, - List of all
 * repositories for relationships defined in resource class. - Parent
 * RegistryEntry if a resource inherits from another resource
 *
 * @param <T> resource type
 */
public class RegistryEntry {

	private final ResourceInformation resourceInformation;

	private final ResourceEntry resourceEntry;

	private final List<ResponseRelationshipEntry> relationshipEntries;

	private RegistryEntry parentRegistryEntry = null;

	private ModuleRegistry moduleRegistry;

	private ResourceRepositoryInformation repositoryInformation;

	public RegistryEntry(ResourceRepositoryInformation repositoryInformation, @SuppressWarnings("SameParameterValue") ResourceEntry resourceEntry) {
		this(repositoryInformation, resourceEntry, new LinkedList<ResponseRelationshipEntry>());
	}

	public RegistryEntry(ResourceRepositoryInformation repositoryInformation, ResourceEntry resourceEntry, List<ResponseRelationshipEntry> relationshipEntries) {
		this.repositoryInformation = repositoryInformation;
		this.resourceInformation = repositoryInformation.getResourceInformation();
		this.resourceEntry = resourceEntry;
		this.relationshipEntries = relationshipEntries;
	}

	public void initialize(ModuleRegistry moduleRegistry) {
		this.moduleRegistry = moduleRegistry;
	}

	@SuppressWarnings("unchecked")
	public ResourceRepositoryAdapter getResourceRepository(RepositoryMethodParameterProvider parameterProvider) {
		Object repoInstance = null;
		if (resourceEntry instanceof DirectResponseResourceEntry) {
			repoInstance = ((DirectResponseResourceEntry) resourceEntry).getResourceRepository();
		} else if (resourceEntry instanceof AnnotatedResourceEntry) {
			repoInstance = ((AnnotatedResourceEntry) resourceEntry).build(parameterProvider);
		}

		if (repoInstance instanceof ResourceRegistryAware) {
			((ResourceRegistryAware) repoInstance).setResourceRegistry(moduleRegistry.getResourceRegistry());
		}

		return new ResourceRepositoryAdapter(resourceInformation, moduleRegistry, repoInstance);
	}

	public List<ResponseRelationshipEntry> getRelationshipEntries() {
		return relationshipEntries;
	}

	@SuppressWarnings("unchecked")
	public RelationshipRepositoryAdapter getRelationshipRepositoryForClass(Class<?> clazz, RepositoryMethodParameterProvider parameterProvider) {
		ResponseRelationshipEntry foundRelationshipEntry = null;
		for (ResponseRelationshipEntry relationshipEntry : relationshipEntries) {
			if (clazz == relationshipEntry.getTargetAffiliation()) {
				foundRelationshipEntry = relationshipEntry;
				break;
			}
		}
		if (foundRelationshipEntry == null) {
			throw new RelationshipRepositoryNotFoundException(resourceInformation.getResourceClass(), clazz);
		}

		Object repoInstance;
		if (foundRelationshipEntry instanceof AnnotatedRelationshipEntryBuilder) {
			repoInstance = ((AnnotatedRelationshipEntryBuilder) foundRelationshipEntry).build(parameterProvider);
		} else {
			repoInstance = ((DirectResponseRelationshipEntry) foundRelationshipEntry).getRepositoryInstanceBuilder();
		}

		if (repoInstance instanceof ResourceRegistryAware) {
			((ResourceRegistryAware) repoInstance).setResourceRegistry(moduleRegistry.getResourceRegistry());
		}

		return new RelationshipRepositoryAdapter(resourceInformation, moduleRegistry, repoInstance);
	}

	public ResourceInformation getResourceInformation() {
		return resourceInformation;
	}

	public ResourceRepositoryInformation getRepositoryInformation() {
		return repositoryInformation;
	}

	public RegistryEntry getParentRegistryEntry() {
		String superResourceType = resourceInformation.getSuperResourceType();
		if (superResourceType != null) {
			ResourceRegistry resourceRegistry = moduleRegistry.getResourceRegistry();
			return resourceRegistry.getEntry(superResourceType);
		}
		return parentRegistryEntry;
	}

	/**
	 * @param parentRegistryEntry parent resource
	 */
	@Deprecated
	public void setParentRegistryEntry(RegistryEntry parentRegistryEntry) {
		this.parentRegistryEntry = parentRegistryEntry;
	}

	/**
	 * Check the legacy is a parent of <b>this</b> {@link RegistryEntry}
	 * instance
	 *
	 * @param registryEntry parent to check
	 * @return true if the legacy is a parent
	 */
	public boolean isParent(RegistryEntry registryEntry) {
		RegistryEntry entry = getParentRegistryEntry();
		while (entry != null) {
			if (entry.equals(registryEntry)) {
				return true;
			}
			entry = entry.getParentRegistryEntry();
		}
		return false;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || !(o instanceof RegistryEntry)) {
			return false;
		}
		RegistryEntry that = (RegistryEntry) o;
		return Objects.equals(resourceInformation, that.resourceInformation) && // NOSONAR
				Objects.equals(repositoryInformation, that.repositoryInformation) && Objects.equals(resourceEntry, that.resourceEntry) && Objects.equals(moduleRegistry, that.moduleRegistry)
				&& Objects.equals(relationshipEntries, that.relationshipEntries) && Objects.equals(parentRegistryEntry, that.parentRegistryEntry);
	}

	@Override
	public int hashCode() {
		return Objects.hash(repositoryInformation, resourceInformation, resourceEntry, relationshipEntries, moduleRegistry, parentRegistryEntry);
	}
}
