package io.crnk.core.engine.internal.registry;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import io.crnk.core.engine.information.repository.ResourceRepositoryInformation;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.repository.RelationshipRepositoryAdapter;
import io.crnk.core.engine.internal.repository.RelationshipRepositoryAdapterImpl;
import io.crnk.core.engine.internal.repository.ResourceRepositoryAdapter;
import io.crnk.core.engine.internal.repository.ResourceRepositoryAdapterImpl;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.engine.registry.ResourceRegistryAware;
import io.crnk.core.engine.registry.ResponseRelationshipEntry;
import io.crnk.core.exception.RelationshipRepositoryNotFoundException;
import io.crnk.core.exception.ResourceFieldNotFoundException;
import io.crnk.core.module.ModuleRegistry;
import io.crnk.core.queryspec.pagingspec.PagingBehavior;
import io.crnk.core.queryspec.pagingspec.PagingSpec;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.legacy.internal.DirectResponseRelationshipEntry;
import io.crnk.legacy.internal.DirectResponseResourceEntry;

/**
 * Holds information about a resource of type <i>T</i> and its repositories. It
 * includes the following information: - ResourceInformation instance with
 * information about the resource, - ResourceEntry instance, - List of all
 * repositories for relationships defined in resource class. - Parent
 * RegistryEntry if a resource inherits from another resource
 */
public class LegacyRegistryEntry implements RegistryEntry {

	@Deprecated
	private ResourceEntry resourceEntry;

	@Deprecated
	private Map<ResourceField, ResponseRelationshipEntry> relationshipEntries;

	private RegistryEntry parentRegistryEntry = null;

	private ModuleRegistry moduleRegistry;

	private PagingBehavior pagingBehavior;

	@Deprecated
	public LegacyRegistryEntry(ResourceEntry resourceEntry) {
		this(resourceEntry, new HashMap<>());
	}

	@Deprecated
	public LegacyRegistryEntry(ResourceEntry resourceEntry, Map<ResourceField, ResponseRelationshipEntry> relationshipEntries) {
		this.resourceEntry = resourceEntry;
		this.relationshipEntries = relationshipEntries;
	}

	public void initialize(ModuleRegistry moduleRegistry) {
		PreconditionUtil.verify(moduleRegistry != null, "no moduleRegistry");
		this.moduleRegistry = moduleRegistry;

		ResourceInformation resourceInformation = getResourceInformation();
		Class<? extends PagingSpec> pagingSpecType = resourceInformation.getPagingSpecType();
		pagingBehavior = moduleRegistry.findPagingBehavior(pagingSpecType);
	}

	@SuppressWarnings("unchecked")
	public ResourceRepositoryAdapter getResourceRepository() {
		Object repoInstance = null;
		if (resourceEntry instanceof DirectResponseResourceEntry) {
			repoInstance = ((DirectResponseResourceEntry) resourceEntry).getResourceRepository();
		}

		if (repoInstance instanceof ResourceRegistryAware) {
			((ResourceRegistryAware) repoInstance).setResourceRegistry(moduleRegistry.getResourceRegistry());
		}

		ResourceRepositoryInformation information = getRepositoryInformation();
		return new ResourceRepositoryAdapterImpl(information, moduleRegistry, repoInstance);
	}

	public RelationshipRepositoryAdapter getRelationshipRepository(String fieldName) {
		ResourceField field = getResourceInformation().findFieldByUnderlyingName(fieldName);
		if (field == null) {
			throw new ResourceFieldNotFoundException("field=" + fieldName);
		}
		return getRelationshipRepository(field);
	}

	@SuppressWarnings("unchecked")
	public RelationshipRepositoryAdapter getRelationshipRepository(ResourceField field) {
		ResponseRelationshipEntry relationshipEntry = relationshipEntries.get(field);
		if (relationshipEntry == null) {
			throw new RelationshipRepositoryNotFoundException(field);
		}

		Object repoInstance = ((DirectResponseRelationshipEntry) relationshipEntry).getRepositoryInstanceBuilder();

		if (repoInstance instanceof ResourceRegistryAware) {
			((ResourceRegistryAware) repoInstance).setResourceRegistry(moduleRegistry.getResourceRegistry());
		}

		return new RelationshipRepositoryAdapterImpl(field, moduleRegistry, repoInstance);
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
	 * Check the legacy is a parent of <b>this</b> {@link LegacyRegistryEntry}
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

	/**
	 * @return {@link ResourceRepository} facade to access the repository. Note that this is not the original
	 * {@link ResourceRepository}
	 * implementation backing the repository, but a facade that will also invoke all filters, decorators, etc. The actual
	 * repository may or may not be implemented with {@link ResourceRepository}.
	 * <p>
	 * Note that currently there is not (yet) any inclusion mechanism supported. This is currently done on a
	 * resource/document level only. But there might be some benefit to also be able to do it here on some occasions.
	 */
	public <T, I > ResourceRepository<T, I> getResourceRepositoryFacade() {
		return (ResourceRepository<T, I>) new ResourceRepositoryFacade(this, moduleRegistry);
	}

	@Override
	public PagingBehavior getPagingBehavior() {
		return pagingBehavior;
	}

	@Override
	public boolean hasResourceRepository() {
		return parentRegistryEntry == null || !parentRegistryEntry.getResourceInformation().getResourcePath().equals(getResourceInformation().getResourcePath());
	}

	public Map<ResourceField, ResponseRelationshipEntry> getRelationshipEntries() {
		return relationshipEntries;
	}


}
