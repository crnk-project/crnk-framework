package io.crnk.legacy.internal;

import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.module.ModuleRegistry;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.legacy.queryParams.QueryParams;
import io.crnk.legacy.queryParams.params.IncludedFieldsParams;
import io.crnk.legacy.queryParams.params.IncludedRelationsParams;
import io.crnk.legacy.queryParams.params.TypedParams;

public class QueryParamsAdapter implements QueryAdapter {

	private QueryParams queryParams;

	private ResourceInformation resourceInformation;

	private ResourceRegistry resourceRegistry;

	private ModuleRegistry moduleRegistry;

	public QueryParamsAdapter(ResourceInformation resourceInformation, QueryParams queryParams, ModuleRegistry
			moduleRegistry) {
		this.queryParams = queryParams;
		this.resourceInformation = resourceInformation;
		this.moduleRegistry = moduleRegistry;
		this.resourceRegistry = moduleRegistry.getResourceRegistry();
	}

	public QueryParamsAdapter(QueryParams queryParams) {
		this.queryParams = queryParams;
	}

	public QueryParams getQueryParams() {
		return queryParams;
	}

	@Override
	public TypedParams<IncludedRelationsParams> getIncludedRelations() {
		return queryParams.getIncludedRelations();
	}

	@Override
	public TypedParams<IncludedFieldsParams> getIncludedFields() {
		return queryParams.getIncludedFields();
	}

	@Override
	public ResourceInformation getResourceInformation() {
		if (resourceInformation == null) {
			throw new IllegalStateException("resourceInformation not set");
		}
		return resourceInformation;
	}

	public ResourceRegistry getResourceRegistry() {
		if (resourceRegistry == null) {
			throw new IllegalStateException("resourceRegistry not set");
		}
		return resourceRegistry;
	}

	@Override
	public Long getLimit() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setLimit(Long limit) {
		throw new UnsupportedOperationException();
	}

	@Override
	public long getOffset() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setOffset(long offset) {
		throw new UnsupportedOperationException();
	}

	@Override
	public QueryAdapter duplicate() {
		throw new UnsupportedOperationException();
	}

	@Override
	public QueryParams toQueryParams() {
		return getQueryParams();
	}

	@Override
	public QuerySpec toQuerySpec() {
		DefaultQuerySpecConverter converter = new DefaultQuerySpecConverter(moduleRegistry);
		return converter.fromParams(getResourceInformation().getResourceClass(), getQueryParams());
	}

	@Override
	public boolean getCompactMode() {
		return false;
	}
}