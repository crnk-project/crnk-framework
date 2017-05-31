package io.crnk.core.queryspec.internal;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.utils.StringUtils;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.queryspec.IncludeFieldSpec;
import io.crnk.core.queryspec.IncludeRelationSpec;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.legacy.queryParams.DefaultQueryParamsConverter;
import io.crnk.legacy.queryParams.QueryParams;
import io.crnk.legacy.queryParams.include.Inclusion;
import io.crnk.legacy.queryParams.params.IncludedFieldsParams;
import io.crnk.legacy.queryParams.params.IncludedRelationsParams;
import io.crnk.legacy.queryParams.params.TypedParams;

public class QuerySpecAdapter implements QueryAdapter {

	private QuerySpec querySpec;

	private ResourceRegistry resourceRegistry;

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
		for (QuerySpec relatedSpec : querySpec.getRelatedSpecs().values()) {
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
			params.put(getResourceTypee(spec), new IncludedRelationsParams(set));
		}
	}

	private String getResourceTypee(QuerySpec spec) {
		RegistryEntry entry = resourceRegistry.findEntry(spec.getResourceClass());
		ResourceInformation resourceInformation = entry.getResourceInformation();
		return resourceInformation.getResourceType();
	}

	@Override
	public TypedParams<IncludedFieldsParams> getIncludedFields() {
		Map<String, IncludedFieldsParams> params = new HashMap<>();
		addFields(params, querySpec);
		for (QuerySpec relatedSpec : querySpec.getRelatedSpecs().values()) {
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
			params.put(getResourceTypee(spec), new IncludedFieldsParams(set));
		}
	}

	@Override
	public ResourceInformation getResourceInformation() {
		return resourceRegistry.findEntry(querySpec.getResourceClass()).getResourceInformation();
	}

	@Override
	public Long getLimit() {
		return querySpec.getLimit();
	}

	@Override
	public void setLimit(Long limit) {
		querySpec.setLimit(limit);
	}

	@Override
	public long getOffset() {
		return querySpec.getOffset();
	}

	@Override
	public void setOffset(long offset) {
		querySpec.setOffset(offset);
	}

	@Override
	public QueryAdapter duplicate() {
		return new QuerySpecAdapter(querySpec.duplicate(), resourceRegistry);
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
}
