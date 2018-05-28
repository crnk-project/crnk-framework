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
import io.crnk.core.repository.ResourceRepositoryV2;
import io.crnk.legacy.internal.RepositoryMethodParameterProvider;
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


	public RegistryEntryImpl(ResourceRepositoryAdapter resourceRepositoryAdapter,
							 Map<ResourceField, RelationshipRepositoryAdapter> relationshipRepositoryAdapters,
							 ModuleRegistry moduleRegistry) {
		this.resourceRepositoryAdapter = resourceRepositoryAdapter;
		this.relationshipRepositoryAdapter = relationshipRepositoryAdapters;
		this.moduleRegistry = moduleRegistry;
		PreconditionUtil.verify(moduleRegistry != null, "no moduleRegistry");
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[type=" + getResourceInformation().getResourceType() + "]";
	}

	@Override
	public ResourceRepositoryAdapter getResourceRepository(RepositoryMethodParameterProvider parameterProvider) {
		return resourceRepositoryAdapter;
	}

	@Override
	public RelationshipRepositoryAdapter getRelationshipRepository(String fieldName, RepositoryMethodParameterProvider
			parameterProvider) {
		ResourceField field = getResourceInformation().findFieldByUnderlyingName(fieldName);
		if (field == null) {
			throw new ResourceFieldNotFoundException("field=" + fieldName);
		}
		return getRelationshipRepository(field, parameterProvider);
	}

	@Override
	public RelationshipRepositoryAdapter getRelationshipRepository(ResourceField field, RepositoryMethodParameterProvider
			parameterProvider) {
		RelationshipRepositoryAdapter adapter = relationshipRepositoryAdapter.get(field);
		if (adapter == null) {
			throw new RelationshipRepositoryNotFoundException(getResourceInformation().getResourceType(),
					field.getUnderlyingName());
		}
		return adapter;
	}

	@Override
	public ResourceInformation getResourceInformation() {
		return resourceRepositoryAdapter.getRepositoryInformation().getResourceInformation().get();
	}

	@Override
	public ResourceRepositoryInformation getRepositoryInformation() {
		return resourceRepositoryAdapter.getRepositoryInformation();
	}

	@Override
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
	 * @return we may or may should not have a public facing ResourceRepositoryAdapter
	 */
	@Deprecated
	public ResourceRepositoryAdapter getResourceRepository() {
		return getResourceRepository(null);
	}

	/**
	 * @return {@link ResourceRepositoryV2} facade to access the repository. Note that this is not the original
	 * {@link ResourceRepositoryV2}
	 * implementation backing the repository, but a facade that will also invoke all filters, decorators, etc. The actual
	 * repository may or may not be implemented with {@link ResourceRepositoryV2}.
	 * <p>
	 * Note that currently there is not (yet) any inclusion mechanism supported. This is currently done on a
	 * resource/document level only. But there might be some benefit to also be able to do it here on some occasions.
	 */
	public <T, I extends Serializable> ResourceRepositoryV2<T, I> getResourceRepositoryFacade() {
		return (ResourceRepositoryV2<T, I>) new ResourceRepositoryFacade(this, moduleRegistry);
	}

}
