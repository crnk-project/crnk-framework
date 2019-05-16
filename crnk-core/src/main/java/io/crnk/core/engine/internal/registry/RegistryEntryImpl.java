package io.crnk.core.engine.internal.registry;

import java.io.Serializable;
import java.util.Map;

import io.crnk.core.engine.information.repository.ResourceRepositoryInformation;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.repository.RelationshipRepositoryAdapter;
import io.crnk.core.engine.internal.repository.ResourceRepositoryAdapter;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.exception.RelationshipRepositoryNotFoundException;
import io.crnk.core.exception.ResourceFieldNotFoundException;
import io.crnk.core.module.ModuleRegistry;
import io.crnk.core.queryspec.pagingspec.PagingBehavior;
import io.crnk.core.queryspec.pagingspec.PagingSpec;
import io.crnk.core.repository.ResourceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Holds information about a resource of type <i>T</i> and its repositories. It
 * includes the following information: - ResourceInformation instance with
 * information about the resource, - ResourceEntry instance, - List of all
 * repositories for relationships defined in resource class. - Parent
 * RegistryEntry if a resource inherits from another resource
 */
public class RegistryEntryImpl implements RegistryEntry {

	private static final Logger LOGGER = LoggerFactory.getLogger(RegistryEntryImpl.class);

	private RegistryEntry parentRegistryEntry = null;

	private ModuleRegistry moduleRegistry;

	private ResourceRepositoryAdapter resourceRepositoryAdapter;

	private Map<ResourceField, RelationshipRepositoryAdapter> relationshipRepositoryAdapter;

	private PagingBehavior pagingBehavior;

	private ResourceInformation resourceInformation;

	/**
	 * Used for resources with repositories (default).
	 */
	public RegistryEntryImpl(ResourceInformation resourceInformation, ResourceRepositoryAdapter resourceRepositoryAdapter,
			Map<ResourceField, RelationshipRepositoryAdapter> relationshipRepositoryAdapters,
			ModuleRegistry moduleRegistry) {
		this.resourceRepositoryAdapter = resourceRepositoryAdapter;
		this.relationshipRepositoryAdapter = relationshipRepositoryAdapters;
		this.moduleRegistry = moduleRegistry;
		this.resourceInformation = resourceInformation;
		PreconditionUtil.verify(resourceInformation != null, "resourceInformation must not be null");
		PreconditionUtil.verify(moduleRegistry != null, "no moduleRegistry");
	}


	@Override
	public String toString() {
		return getClass().getSimpleName() + "[type=" + resourceInformation.getResourceType() + ", path=" + resourceInformation.getResourcePath() + "]";
	}

	@Override
	public ResourceRepositoryAdapter getResourceRepository() {
		if (resourceRepositoryAdapter != null) {
			return resourceRepositoryAdapter;
		}
		return parentRegistryEntry.getResourceRepository();
	}

	@Override
	public RelationshipRepositoryAdapter getRelationshipRepository(String fieldName) {
		ResourceField field = getResourceInformation().findFieldByUnderlyingName(fieldName);
		if (field == null && parentRegistryEntry != null) {
			return parentRegistryEntry.getRelationshipRepository(fieldName);
		}
		if (field == null) {
			throw new ResourceFieldNotFoundException("name=" + fieldName);
		}
		return getRelationshipRepository(field);
	}

	@Override
	public RelationshipRepositoryAdapter getRelationshipRepository(ResourceField field) {
		RelationshipRepositoryAdapter adapter = relationshipRepositoryAdapter.get(field);
		if (adapter == null && parentRegistryEntry != null) {
			return parentRegistryEntry.getRelationshipRepository(field);
		}
		if (adapter == null) {
			throw new RelationshipRepositoryNotFoundException(field);
		}
		return adapter;
	}

	@Override
	public ResourceInformation getResourceInformation() {
		return resourceInformation;
	}

	@Override
	public ResourceRepositoryInformation getRepositoryInformation() {
		return resourceRepositoryAdapter != null ? resourceRepositoryAdapter.getRepositoryInformation() : null;
	}

	@Override
	public RegistryEntry getParentRegistryEntry() {
		if (parentRegistryEntry != null) {
			return parentRegistryEntry;
		}
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
	 * Check the legacy is a parent of <b>this</b> {@link RegistryEntryImpl}
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
		if (pagingBehavior == null) {
			ResourceInformation resourceInformation = getResourceInformation();
			Class<? extends PagingSpec> pagingSpecType = resourceInformation.getPagingSpecType();
			pagingBehavior = moduleRegistry.findPagingBehavior(pagingSpecType);
		}
		return pagingBehavior;
	}

	@Override
	public boolean hasResourceRepository() {
		RegistryEntry parent = getParentRegistryEntry();
		return parent == null || !parent.getResourceInformation().getResourcePath().equals(getResourceInformation().getResourcePath());
	}

}
