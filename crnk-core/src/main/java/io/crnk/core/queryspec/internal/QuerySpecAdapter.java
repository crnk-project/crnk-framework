package io.crnk.core.queryspec.internal;

import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.utils.StringUtils;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.queryspec.IncludeFieldSpec;
import io.crnk.core.queryspec.IncludeRelationSpec;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.queryspec.pagingspec.PagingSpec;
import io.crnk.legacy.queryParams.DefaultQueryParamsConverter;
import io.crnk.legacy.queryParams.QueryParams;
import io.crnk.legacy.queryParams.include.Inclusion;
import io.crnk.legacy.queryParams.params.IncludedFieldsParams;
import io.crnk.legacy.queryParams.params.IncludedRelationsParams;
import io.crnk.legacy.queryParams.params.TypedParams;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class QuerySpecAdapter implements QueryAdapter {

	private QuerySpec querySpec;

	private ResourceRegistry resourceRegistry;

	private boolean compactMode;

	public QuerySpecAdapter(QuerySpec querySpec, ResourceRegistry resourceRegistry) {
		this.querySpec = querySpec;
		this.resourceRegistry = resourceRegistry;
	}

	public QuerySpec getQuerySpec() {
		return querySpec;
	}

	@Override
	public TypedParams<IncludedRelationsParams> getIncludedRelations() {
		Map<String, IncludedRelationsParams> params = new HashMap<>();
		addRelations(params, querySpec);
		for (QuerySpec relatedSpec : querySpec.getNestedSpecs()) {
			addRelations(params, relatedSpec);
		}
		return new TypedParams<>(params);
	}

	private void addRelations(Map<String, IncludedRelationsParams> params, QuerySpec spec) {
		if (!spec.getIncludedRelations().isEmpty()) {
			Set<Inclusion> set = new HashSet<>();
			for (IncludeRelationSpec relation : spec.getIncludedRelations()) {
				set.add(new Inclusion(StringUtils.join(".", relation.getAttributePath())));
			}
			params.put(getResourceType(spec), new IncludedRelationsParams(set));
		}
	}

	private String getResourceType(QuerySpec spec) {
		if (spec.getResourceType() != null) {
			return spec.getResourceType();
		}
		RegistryEntry entry = resourceRegistry.getEntry(spec.getResourceClass());
		ResourceInformation resourceInformation = entry.getResourceInformation();
		return resourceInformation.getResourceType();
	}

	@Override
	public TypedParams<IncludedFieldsParams> getIncludedFields() {
		Map<String, IncludedFieldsParams> params = new HashMap<>();
		addFields(params, querySpec);
		for (QuerySpec relatedSpec : querySpec.getNestedSpecs()) {
			addFields(params, relatedSpec);
		}
		return new TypedParams<>(params);
	}

	private void addFields(Map<String, IncludedFieldsParams> params, QuerySpec spec) {
		if (!spec.getIncludedFields().isEmpty()) {
			Set<String> set = new HashSet<>();
			for (IncludeFieldSpec relation : spec.getIncludedFields()) {
				set.add(StringUtils.join(".", relation.getAttributePath()));
			}
			params.put(getResourceType(spec), new IncludedFieldsParams(set));
		}
	}

	@Override
	public ResourceInformation getResourceInformation() {
		return resourceRegistry.getEntry(getResourceType(querySpec)).getResourceInformation();
	}

	@Override
	public QueryAdapter duplicate() {
		QuerySpecAdapter adapter = new QuerySpecAdapter(querySpec.duplicate(), resourceRegistry);
		adapter.setCompactMode(compactMode);
		return adapter;
	}

	@Override
	public QueryParams toQueryParams() {
		DefaultQueryParamsConverter converter = new DefaultQueryParamsConverter(resourceRegistry);
		return converter.fromParams(getResourceInformation().getResourceClass(), getQuerySpec());
	}

	@Override
	public QuerySpec toQuerySpec() {
		return getQuerySpec();
	}

	@Override
	public boolean getCompactMode() {
		return compactMode;
	}

	@Override
	public void setPagingSpec(final PagingSpec pagingSpec) {
		querySpec.setPagingSpec(pagingSpec);
	}

	@Override
	public PagingSpec getPagingSpec() {
		return querySpec.getPagingSpec();
	}

	public void setCompactMode(boolean compactMode) {
		this.compactMode = compactMode;
	}
}
