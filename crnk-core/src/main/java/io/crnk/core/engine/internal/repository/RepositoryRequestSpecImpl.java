package io.crnk.core.engine.internal.repository;

import io.crnk.core.engine.dispatcher.RepositoryRequestSpec;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.engine.internal.utils.StringUtils;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.module.ModuleRegistry;
import io.crnk.core.queryspec.IncludeFieldSpec;
import io.crnk.core.queryspec.IncludeRelationSpec;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.queryspec.internal.QuerySpecAdapter;
import io.crnk.legacy.internal.DefaultQuerySpecConverter;
import io.crnk.legacy.internal.QueryParamsAdapter;
import io.crnk.legacy.queryParams.DefaultQueryParamsParser;
import io.crnk.legacy.queryParams.QueryParams;
import io.crnk.legacy.queryParams.QueryParamsBuilder;

import java.io.Serializable;
import java.util.*;

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

	public static RepositoryRequestSpec forSave(ModuleRegistry moduleRegistry, HttpMethod method, QueryAdapter queryAdapter, Object entity) {
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

	public static RepositoryRequestSpec forFindTarget(ModuleRegistry moduleRegistry, QueryAdapter queryAdapter, List<?> ids, ResourceField relationshipField) {
		RepositoryRequestSpecImpl spec = new RepositoryRequestSpecImpl(moduleRegistry);
		spec.queryAdapter = queryAdapter;
		spec.ids = ids;
		spec.relationshipField = relationshipField;
		spec.method = HttpMethod.GET;
		PreconditionUtil.assertNotNull("relationshipField is null", relationshipField);
		return spec;
	}

	public static RepositoryRequestSpecImpl forRelation(ModuleRegistry moduleRegistry, HttpMethod method, Object entity, QueryAdapter queryAdapter, Iterable<?> ids, ResourceField relationshipField) {
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
		if (queryAdapter == null)
			return null;
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
		if (queryAdapter == null)
			return null;
		if (!(queryAdapter instanceof QueryParamsAdapter)) {
			QuerySpec rootQuerySpec = ((QuerySpecAdapter) queryAdapter).getQuerySpec();
			return convertToQueryParams(rootQuerySpec);
		}
		return ((QueryParamsAdapter) queryAdapter).getQueryParams();
	}

	private QueryParams convertToQueryParams(QuerySpec rootQuerySpec) {
		Map<String, Set<String>> map = new HashMap<>();
		List<QuerySpec> querySpecs = new ArrayList<>();
		querySpecs.addAll(rootQuerySpec.getRelatedSpecs().values());
		querySpecs.add(rootQuerySpec);
		for (QuerySpec spec : querySpecs) {
			if (!spec.getFilters().isEmpty() || !spec.getSort().isEmpty() || spec.getLimit() != null || spec.getOffset() != 0) {
				throw new UnsupportedOperationException(); // not
				// implemented
			}

			String resourceType = moduleRegistry.getResourceRegistry().findEntry(spec.getResourceClass()).getResourceInformation().getResourceType();
			if (!spec.getIncludedFields().isEmpty()) {
				Set<String> fieldNames = new HashSet<>();
				for (IncludeFieldSpec field : spec.getIncludedFields()) {
					fieldNames.add(StringUtils.join(".", field.getAttributePath()));
				}
				map.put("fields[" + resourceType + "]", fieldNames);
			}

			if (!spec.getIncludedRelations().isEmpty()) {
				Set<String> fieldNames = new HashSet<>();
				for (IncludeRelationSpec field : spec.getIncludedRelations()) {
					fieldNames.add(StringUtils.join(".", field.getAttributePath()));
				}
				map.put("include[" + resourceType + "]", fieldNames);
			}
		}

		QueryParamsBuilder queryParamsBuilder = new QueryParamsBuilder(new DefaultQueryParamsParser());
		return queryParamsBuilder.buildQueryParams(map);
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