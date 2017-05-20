package io.crnk.legacy.internal;

import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.query.QueryAdapterBuilder;
import io.crnk.core.module.ModuleRegistry;
import io.crnk.legacy.queryParams.QueryParamsBuilder;
import io.crnk.legacy.queryParams.context.SimpleQueryParamsParserContext;

import java.util.Map;
import java.util.Set;

public class QueryParamsAdapterBuilder implements QueryAdapterBuilder {

	private QueryParamsBuilder queryParamsBuilder;
	private ModuleRegistry moduleRegistry;

	public QueryParamsAdapterBuilder(QueryParamsBuilder queryParamsBuilder, ModuleRegistry moduleRegistry) {
		this.queryParamsBuilder = queryParamsBuilder;
		this.moduleRegistry = moduleRegistry;
	}

	@Override
	public QueryAdapter build(ResourceInformation info, Map<String, Set<String>> parameters) {
		SimpleQueryParamsParserContext context = new SimpleQueryParamsParserContext(parameters, info);
		return new QueryParamsAdapter(info, queryParamsBuilder.buildQueryParams(context), moduleRegistry);
	}
}