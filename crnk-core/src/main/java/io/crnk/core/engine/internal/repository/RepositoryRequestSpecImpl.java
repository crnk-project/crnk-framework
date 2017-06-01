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
import io.crnk.core.module.ModuleRegistry;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.queryspec.internal.QuerySpecAdapter;
import io.crnk.legacy.internal.DefaultQuerySpecConverter;
import io.crnk.legacy.queryParams.QueryParams;

/**
 * Add some point maybe a more prominent api is necessary for this. But i likely
 * should be keept separate from QuerySpec.
 */
class RepositoryRequestSpecImpl implements RepositoryRequestSpec {

	private ResourceField relationshipField;

	private QueryAdapter queryAdapter;

	private Iterable<?> ids;

	private Object entity;

	private ModuleRegistry moduleRegistry;

	private HttpMethod method;

	private RepositoryRequestSpecImpl(ModuleRegistry moduleRegistry) {
		this.moduleRegistry = moduleRegistry;

	}

	public static RepositoryRequestSpec forDelete(ModuleRegistry moduleRegistry, QueryAdapter queryAdapter, Serializable id) {
		RepositoryRequestSpecImpl spec = new RepositoryRequestSpecImpl(moduleRegistry);
		spec.queryAdapter = queryAdapter;
		spec.ids = Arrays.asList(id);
		spec.method = HttpMethod.DELETE;
		return spec;
	}

	public static RepositoryRequestSpec forSave(ModuleRegistry moduleRegistry, HttpMethod method, QueryAdapter queryAdapter,
			Object entity) {
		RepositoryRequestSpecImpl spec = new RepositoryRequestSpecImpl(moduleRegistry);
		spec.queryAdapter = queryAdapter;
		spec.entity = entity;
		spec.method = method;
		return spec;
	}

	public static RepositoryRequestSpec forFindIds(ModuleRegistry moduleRegistry, QueryAdapter queryAdapter, Iterable<?> ids) {
		RepositoryRequestSpecImpl spec = new RepositoryRequestSpecImpl(moduleRegistry);
		spec.queryAdapter = queryAdapter;
		spec.ids = ids;
		spec.method = HttpMethod.GET;
		return spec;
	}

	public static RepositoryRequestSpec forFindAll(ModuleRegistry moduleRegistry, QueryAdapter queryAdapter) {
		RepositoryRequestSpecImpl spec = new RepositoryRequestSpecImpl(moduleRegistry);
		spec.queryAdapter = queryAdapter;
		spec.method = HttpMethod.GET;
		return spec;
	}

	public static RepositoryRequestSpec forFindId(ModuleRegistry moduleRegistry, QueryAdapter queryAdapter, Serializable id) {
		RepositoryRequestSpecImpl spec = new RepositoryRequestSpecImpl(moduleRegistry);
		spec.queryAdapter = queryAdapter;
		spec.ids = Collections.singleton(id);
		spec.method = HttpMethod.GET;
		return spec;
	}

	public static RepositoryRequestSpec forFindTarget(ModuleRegistry moduleRegistry, QueryAdapter queryAdapter, List<?> ids,
			ResourceField relationshipField) {
		RepositoryRequestSpecImpl spec = new RepositoryRequestSpecImpl(moduleRegistry);
		spec.queryAdapter = queryAdapter;
		spec.ids = ids;
		spec.relationshipField = relationshipField;
		spec.method = HttpMethod.GET;
		PreconditionUtil.assertNotNull("relationshipField is null", relationshipField);
		return spec;
	}

	public static RepositoryRequestSpecImpl forRelation(ModuleRegistry moduleRegistry, HttpMethod method, Object entity,
			QueryAdapter queryAdapter, Iterable<?> ids, ResourceField relationshipField) {
		RepositoryRequestSpecImpl spec = new RepositoryRequestSpecImpl(moduleRegistry);
		spec.entity = entity;
		spec.queryAdapter = queryAdapter;
		spec.ids = ids;
		spec.relationshipField = relationshipField;
		spec.method = method;
		PreconditionUtil.assertNotNull("relationshipField is null", relationshipField);
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
	public QuerySpec getQuerySpec(ResourceInformation targetResourceInformation) {
		if (queryAdapter == null) {
			return null;
		}
		Class<?> targetResourceClass = targetResourceInformation.getResourceClass();
		if (queryAdapter instanceof QuerySpecAdapter) {
			QuerySpec querySpec = ((QuerySpecAdapter) queryAdapter).getQuerySpec();
			return querySpec.getOrCreateQuerySpec(targetResourceClass);
		}
		QueryParams queryParams = getQueryParams();
		DefaultQuerySpecConverter converter = new DefaultQuerySpecConverter(moduleRegistry);
		return converter.fromParams(targetResourceClass, queryParams);
	}

	@Override
	public QueryParams getQueryParams() {
		if (queryAdapter == null) {
			return null;
		}

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