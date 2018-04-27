package io.crnk.core.queryspec.internal;

import io.crnk.core.engine.http.HttpHeaders;
import io.crnk.core.engine.http.HttpRequestContext;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.query.QueryAdapterBuilder;
import io.crnk.core.engine.query.QueryContext;
import io.crnk.core.module.ModuleRegistry;
import io.crnk.core.queryspec.mapper.QuerySpecUrlMapper;

import java.util.Map;
import java.util.Set;

public class QuerySpecAdapterBuilder implements QueryAdapterBuilder {


	private QuerySpecUrlMapper querySpecUrlMapper;

	private ModuleRegistry moduleRegistry;

	public QuerySpecAdapterBuilder(final QuerySpecUrlMapper querySpecUrlMapper,
								   final ModuleRegistry moduleRegistry) {
		this.moduleRegistry = moduleRegistry;
		this.querySpecUrlMapper = querySpecUrlMapper;
	}

	@Override
	public QueryAdapter build(ResourceInformation resourceInformation, Map<String, Set<String>> parameters,
							  QueryContext queryContext) {
		QuerySpecAdapter adapter = new QuerySpecAdapter(querySpecUrlMapper.deserialize(resourceInformation, parameters),
				moduleRegistry.getResourceRegistry(), queryContext);
		HttpRequestContext requestContext = moduleRegistry.getHttpRequestContextProvider().getRequestContext();
		if (requestContext != null) {
			adapter.setCompactMode(Boolean.parseBoolean(requestContext.getRequestHeader(HttpHeaders.HTTP_HEADER_CRNK_COMPACT)));
		}
		return adapter;
	}
}
