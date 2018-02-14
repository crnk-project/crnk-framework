package io.crnk.core.engine.registry;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import io.crnk.core.engine.document.ErrorData;
import io.crnk.core.engine.error.ErrorResponse;
import io.crnk.core.engine.error.ExceptionMapper;
import io.crnk.core.engine.information.repository.ResourceRepositoryInformation;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.exception.ExceptionMapperRegistry;
import io.crnk.core.engine.internal.repository.RelationshipRepositoryAdapter;
import io.crnk.core.engine.internal.repository.ResourceRepositoryAdapter;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.exception.RelationshipRepositoryNotFoundException;
import io.crnk.core.exception.ResourceFieldNotFoundException;
import io.crnk.core.module.ModuleRegistry;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.queryspec.internal.QuerySpecAdapter;
import io.crnk.core.repository.ResourceRepositoryV2;
import io.crnk.core.repository.response.JsonApiResponse;
import io.crnk.core.resource.list.DefaultResourceList;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.legacy.internal.DirectResponseRelationshipEntry;
import io.crnk.legacy.internal.DirectResponseResourceEntry;
import io.crnk.legacy.internal.RepositoryMethodParameterProvider;
import io.crnk.legacy.registry.AnnotatedRelationshipEntryBuilder;
import io.crnk.legacy.registry.AnnotatedResourceEntry;

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
		return (ResourceRepositoryV2<T, I>) new ResourceRepositoryFacade();
	}

	public Map<ResourceField, ResponseRelationshipEntry> getRelationshipEntries() {
		return relationshipEntries;
	}

	class ResourceRepositoryFacade implements ResourceRepositoryV2<Object, Serializable> {

		@Override
		public Class getResourceClass() {
			return getResourceInformation().getResourceClass();
		}

		@Override
		public Object findOne(Serializable id, QuerySpec querySpec) {
			ResourceRepositoryAdapter adapter = getResourceRepository();
			return toResource(adapter.findOne(id, toAdapter(querySpec)));
		}


		@Override
		public ResourceList findAll(QuerySpec querySpec) {
			ResourceRepositoryAdapter adapter = getResourceRepository();
			return (ResourceList) toResources(adapter.findAll(toAdapter(querySpec)));
		}

		@Override
		public ResourceList findAll(Iterable ids, QuerySpec querySpec) {
			ResourceRepositoryAdapter adapter = getResourceRepository();
			return (ResourceList) toResources(adapter.findAll(ids, toAdapter(querySpec)));
		}

		@Override
		public Object save(Object resource) {
			ResourceRepositoryAdapter adapter = getResourceRepository();
			return toResource(adapter.update(resource, createEmptyAdapter()));
		}

		@Override
		public Object create(Object resource) {
			ResourceRepositoryAdapter adapter = getResourceRepository();
			return toResource(adapter.create(resource, createEmptyAdapter()));
		}

		@Override
		public void delete(Serializable id) {
			ResourceRepositoryAdapter adapter = getResourceRepository();
			toResource(adapter.delete(id, createEmptyAdapter()));
		}

		private QueryAdapter createEmptyAdapter() {
			return toAdapter(new QuerySpec(getResourceClass()));
		}

		private QueryAdapter toAdapter(QuerySpec querySpec) {
			return new QuerySpecAdapter(querySpec, moduleRegistry.getResourceRegistry());
		}
	}


	private ResourceList toResources(JsonApiResponse response) {
		Collection elements = (Collection) toResource(response);

		DefaultResourceList result = new DefaultResourceList();
		result.addAll(elements);
		result.setMeta(response.getMetaInformation());
		result.setLinks(response.getLinksInformation());
		return result;
	}

	private Object toResource(JsonApiResponse response) {
		if (response.getErrors() != null && response.getErrors().iterator().hasNext()) {

			List<ErrorData> errorList = new ArrayList<>();
			response.getErrors().forEach(it -> errorList.add(it));
			Optional<Integer> errorCode = errorList.stream().filter(it -> it.getStatus() != null)
					.map(it -> Integer.parseInt(it.getStatus()))
					.collect(Collectors.maxBy(Integer::compare));

			ErrorResponse errorResponse = new ErrorResponse(errorList, errorCode.get());

			ExceptionMapperRegistry exceptionMapperRegistry = moduleRegistry.getExceptionMapperRegistry();
			ExceptionMapper<Throwable> exceptionMapper = exceptionMapperRegistry.findMapperFor(errorResponse).get();
			return exceptionMapper.fromErrorResponse(errorResponse);
		}
		return response.getEntity();

	}
}
