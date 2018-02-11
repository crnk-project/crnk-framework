package io.crnk.core.engine.registry;

import io.crnk.core.engine.information.repository.ResourceRepositoryInformation;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.repository.RelationshipRepositoryAdapter;
import io.crnk.core.engine.internal.repository.ResourceRepositoryAdapter;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.exception.RelationshipRepositoryNotFoundException;
import io.crnk.core.exception.ResourceFieldNotFoundException;
import io.crnk.core.module.ModuleRegistry;
import io.crnk.legacy.internal.DirectResponseRelationshipEntry;
import io.crnk.legacy.internal.DirectResponseResourceEntry;
import io.crnk.legacy.internal.RepositoryMethodParameterProvider;
import io.crnk.legacy.registry.AnnotatedRelationshipEntryBuilder;
import io.crnk.legacy.registry.AnnotatedResourceEntry;
import java.util.HashMap;
import java.util.Map;

/**
 * Holds information about a resource of type <i>T</i> and its repositories. It
 * includes the following information: - ResourceInformation instance with
 * information about the resource, - ResourceEntry instance, - List of all
 * repositories for relationships defined in resource class. - Parent
 * RegistryEntry if a resource inherits from another resource
 */
public class RegistryEntry {

	private final ResourceEntry resourceEntry;

	private final Map<ResourceField, ResponseRelationshipEntry> relationshipEntries;

	private RegistryEntry parentRegistryEntry = null;

	private ModuleRegistry moduleRegistry;

	public RegistryEntry(ResourceEntry resourceEntry) {
		this(resourceEntry, new HashMap<>());
	}

	public RegistryEntry(ResourceEntry resourceEntry, Map<ResourceField, ResponseRelationshipEntry> relationshipEntries) {
		this.resourceEntry = resourceEntry;
		this.relationshipEntries = relationshipEntries;
	}

	public void initialize(ModuleRegistry moduleRegistry) {
		PreconditionUtil.assertNotNull("no moduleRegistry", moduleRegistry);
		this.moduleRegistry = moduleRegistry;
	}

	@SuppressWarnings("unchecked")
	public ResourceRepositoryAdapter getResourceRepository(RepositoryMethodParameterProvider parameterProvider) {
		Object repoInstance = null;
		if (resourceEntry instanceof DirectResponseResourceEntry) {
			repoInstance = ((DirectResponseResourceEntry) resourceEntry).getResourceRepository();
		}
		else if (resourceEntry instanceof AnnotatedResourceEntry) {
			repoInstance = ((AnnotatedResourceEntry) resourceEntry).build(parameterProvider);
		}

		if (repoInstance instanceof ResourceRegistryAware) {
			((ResourceRegistryAware) repoInstance).setResourceRegistry(moduleRegistry.getResourceRegistry());
		}

		ResourceInformation resourceInformation = getResourceInformation();
		return new ResourceRepositoryAdapter(resourceInformation, moduleRegistry, repoInstance);
	}

	public RelationshipRepositoryAdapter getRelationshipRepository(String fieldName, RepositoryMethodParameterProvider
			parameterProvider) {
		ResourceField field = getResourceInformation().findFieldByUnderlyingName(fieldName);
		if (field == null) {
			throw new ResourceFieldNotFoundException("field=" + fieldName);
		}
		return getRelationshipRepository(field, parameterProvider);
	}

	@SuppressWarnings("unchecked")
	public RelationshipRepositoryAdapter getRelationshipRepository(ResourceField field, RepositoryMethodParameterProvider
			parameterProvider) {
		ResponseRelationshipEntry relationshipEntry = relationshipEntries.get(field);
		if (relationshipEntry == null) {
			throw new RelationshipRepositoryNotFoundException(getResourceInformation().getResourceType(),
					field.getUnderlyingName());
		}

		Object repoInstance;
		if (relationshipEntry instanceof AnnotatedRelationshipEntryBuilder) {
			repoInstance = ((AnnotatedRelationshipEntryBuilder) relationshipEntry).build(parameterProvider);
		}
		else {
			repoInstance = ((DirectResponseRelationshipEntry) relationshipEntry).getRepositoryInstanceBuilder();
		}

		if (repoInstance instanceof ResourceRegistryAware) {
			((ResourceRegistryAware) repoInstance).setResourceRegistry(moduleRegistry.getResourceRegistry());
		}

		return new RelationshipRepositoryAdapter(getResourceInformation(), moduleRegistry, repoInstance);
	}

	public ResourceInformation getResourceInformation() {
		return resourceEntry.getRepositoryInformation().getResourceInformation().get();
	}

	public ResourceRepositoryInformation getRepositoryInformation() {
		return resourceEntry.getRepositoryInformation();
	}

	public RegistryEntry getParentRegistryEntry() {
		ResourceInformation resourceInformation = getResourceInformation();
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

	public ResourceRepositoryAdapter getResourceRepository() {
		return getResourceRepository(null);
	}

	public Map<ResourceField, ResponseRelationshipEntry> getRelationshipEntries() {
		return relationshipEntries;
	}
}
