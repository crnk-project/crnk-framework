package io.crnk.core.queryspec.internal;

import java.util.Map;
import java.util.Set;

import io.crnk.core.engine.http.HttpHeaders;
import io.crnk.core.engine.http.HttpRequestContext;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.parser.TypeParser;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.query.QueryAdapterBuilder;
import io.crnk.core.engine.query.QueryContext;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.module.ModuleRegistry;
import io.crnk.core.queryspec.QuerySpecDeserializer;
import io.crnk.core.queryspec.QuerySpecDeserializerContext;

public class QuerySpecAdapterBuilder implements QueryAdapterBuilder {


	private QuerySpecDeserializer querySpecDeserializer;

	private ModuleRegistry moduleRegistry;

	public QuerySpecAdapterBuilder(final QuerySpecDeserializer querySpecDeserializer,
								   final ModuleRegistry moduleRegistry) {
		this.querySpecDeserializer = querySpecDeserializer;
		this.moduleRegistry = moduleRegistry;
		this.querySpecDeserializer.init(new QuerySpecDeserializerContext() {

			@Override
			public ResourceRegistry getResourceRegistry() {
				return moduleRegistry.getResourceRegistry();
			}

			@Override
			public TypeParser getTypeParser() {
				return moduleRegistry.getTypeParser();
			}
		});
	}

	@Override
	public QueryAdapter build(ResourceInformation resourceInformation, Map<String, Set<String>> parameters, QueryContext queryContext) {
		QuerySpecAdapter adapter = new QuerySpecAdapter(querySpecDeserializer.deserialize(resourceInformation, parameters),
				moduleRegistry.getResourceRegistry(), queryContext);
		HttpRequestContext requestContext = moduleRegistry.getHttpRequestContextProvider().getRequestContext();
		if (requestContext != null) {
			adapter.setCompactMode(Boolean.parseBoolean(requestContext.getRequestHeader(HttpHeaders.HTTP_HEADER_CRNK_COMPACT)));
		}
		return adapter;
	}
}
