package io.crnk.core.engine.internal.repository;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import io.crnk.core.engine.dispatcher.RepositoryRequestSpec;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.module.ModuleRegistry;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.queryspec.internal.QuerySpecAdapter;
import io.crnk.legacy.internal.DefaultQuerySpecConverter;
import io.crnk.legacy.queryParams.QueryParams;

/**
 * Add some point maybe a more prominent api is necessary for this. But i likely
 * should be keept separate from QuerySpec.
 */
public class RepositoryRequestSpecImpl implements RepositoryRequestSpec {

	private ResourceField relationshipField;

	private QueryAdapter queryAdapter;

	private Iterable<?> ids;

	private Object entity;

	private ModuleRegistry moduleRegistry;

	private HttpMethod method;

	private ResourceInformation owningResourceInformation;

	private RepositoryRequestSpecImpl(ModuleRegistry moduleRegistry) {
		this.moduleRegistry = moduleRegistry;

	}

	public static RepositoryRequestSpec forDelete(ModuleRegistry moduleRegistry, ResourceInformation owningResourceInformation,
												  QueryAdapter queryAdapter, Serializable id) {
		RepositoryRequestSpecImpl spec = new RepositoryRequestSpecImpl(moduleRegistry);
		spec.queryAdapter = queryAdapter;
		spec.ids = Arrays.asList(id);
		spec.owningResourceInformation = owningResourceInformation;
		spec.method = HttpMethod.DELETE;
		return spec;
	}

	public static RepositoryRequestSpec forSave(ModuleRegistry moduleRegistry, HttpMethod method,
												ResourceInformation owningResourceInformation, QueryAdapter queryAdapter,
												Object entity) {
		RepositoryRequestSpecImpl spec = new RepositoryRequestSpecImpl(moduleRegistry);
		spec.queryAdapter = queryAdapter;
		spec.entity = entity;
		spec.owningResourceInformation = owningResourceInformation;
		spec.method = method;
		return spec;
	}

	public static RepositoryRequestSpec forFindIds(ModuleRegistry moduleRegistry, ResourceInformation owningResourceInformation,
												   QueryAdapter queryAdapter, Iterable<?> ids) {
		RepositoryRequestSpecImpl spec = new RepositoryRequestSpecImpl(moduleRegistry);
		spec.queryAdapter = queryAdapter;
		spec.ids = ids;
		spec.owningResourceInformation = owningResourceInformation;
		spec.method = HttpMethod.GET;
		return spec;
	}

	public static RepositoryRequestSpec forFindAll(ModuleRegistry moduleRegistry, ResourceInformation owningResourceInformation,
												   QueryAdapter queryAdapter) {
		RepositoryRequestSpecImpl spec = new RepositoryRequestSpecImpl(moduleRegistry);
		spec.queryAdapter = queryAdapter;
		spec.owningResourceInformation = owningResourceInformation;
		spec.method = HttpMethod.GET;
		return spec;
	}

	public static RepositoryRequestSpec forFindId(ModuleRegistry moduleRegistry, ResourceInformation owningResourceInformation,
												  QueryAdapter queryAdapter, Serializable id) {
		RepositoryRequestSpecImpl spec = new RepositoryRequestSpecImpl(moduleRegistry);
		spec.queryAdapter = queryAdapter;
		spec.ids = Collections.singleton(id);
		spec.owningResourceInformation = owningResourceInformation;
		spec.method = HttpMethod.GET;
		return spec;
	}

	public static RepositoryRequestSpec forFindTarget(ModuleRegistry moduleRegistry,
													  QueryAdapter queryAdapter, List<?> ids,
													  ResourceField relationshipField) {
		RepositoryRequestSpecImpl spec = new RepositoryRequestSpecImpl(moduleRegistry);
		spec.queryAdapter = queryAdapter;
		spec.ids = ids;
		spec.relationshipField = relationshipField;
		spec.owningResourceInformation = relationshipField.getParentResourceInformation();
		spec.method = HttpMethod.GET;
		PreconditionUtil.verify(relationshipField != null, "relationshipField is null");
		return spec;
	}

	public static RepositoryRequestSpecImpl forRelation(ModuleRegistry moduleRegistry, HttpMethod method, Object entity,
														QueryAdapter queryAdapter, Iterable<?> ids, ResourceField relationshipField) {
		RepositoryRequestSpecImpl spec = new RepositoryRequestSpecImpl(moduleRegistry);
		spec.entity = entity;
		spec.queryAdapter = queryAdapter;
		spec.ids = ids;
		spec.relationshipField = relationshipField;
		spec.owningResourceInformation = relationshipField.getParentResourceInformation();
		spec.method = method;
		PreconditionUtil.verify(relationshipField != null, "relationshipField is null");
		return spec;
	}

	@Override
	public HttpMethod getMethod() {
		return method;
	}

	@Override
	public QueryAdapter getQueryAdapter() {
		return queryAdapter;
	}

	@Override
	public ResourceField getRelationshipField() {
		return relationshipField;
	}

	@Override
	public QuerySpec getResponseQuerySpec() {
		ResourceInformation responseResourceInformation = getResponseResourceInformation();
		return getQuerySpec(responseResourceInformation);
	}

	@Override
	public ResourceInformation getResponseResourceInformation() {
		if (relationshipField != null) {
			String oppositeResourceType = relationshipField.getOppositeResourceType();
			ResourceRegistry resourceRegistry = moduleRegistry.getResourceRegistry();
			RegistryEntry entry = resourceRegistry.getEntry(oppositeResourceType);
			return entry.getResourceInformation();
		}
		return owningResourceInformation;
	}

	@Override
	public ResourceInformation getOwningResourceInformation() {
		return owningResourceInformation;
	}

	@Override
	public QuerySpec getQuerySpec(ResourceInformation targetResourceInformation) {
		if (queryAdapter instanceof QuerySpecAdapter) {
			QuerySpec querySpec = ((QuerySpecAdapter) queryAdapter).getQuerySpec();
			return querySpec.getOrCreateQuerySpec(targetResourceInformation);
		}
		Class<?> targetResourceClass = targetResourceInformation.getResourceClass();
		QueryParams queryParams = getQueryParams();
		DefaultQuerySpecConverter converter = new DefaultQuerySpecConverter(moduleRegistry);
		return converter.fromParams(targetResourceClass, queryParams);
	}

	@Override
	public QueryParams getQueryParams() {
		return queryAdapter.toQueryParams();
	}

	@Override
	public Serializable getId() {
		Iterable<Object> iterable = getIds();
		if (iterable != null) {
			Iterator<?> iterator = iterable.iterator();
			if (iterator.hasNext()) {
				return (Serializable) iterator.next();
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> Iterable<T> getIds() {
		if (ids == null && entity != null) {
			ResourceInformation resourceInformation = queryAdapter.getResourceInformation();
			return (Iterable<T>) Collections.singleton(resourceInformation.getId(entity));
		}
		return (Iterable<T>) ids;
	}

	@Override
	public Object getEntity() {
		return entity;
	}

}