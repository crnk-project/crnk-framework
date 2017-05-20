package io.crnk.core.queryspec.internal;

import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.parser.TypeParser;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.query.QueryAdapterBuilder;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.module.ModuleRegistry;
import io.crnk.core.queryspec.QuerySpecDeserializer;
import io.crnk.core.queryspec.QuerySpecDeserializerContext;

import java.util.Map;
import java.util.Set;

public class QuerySpecAdapterBuilder implements QueryAdapterBuilder {

	private QuerySpecDeserializer querySpecDeserializer;

	private ResourceRegistry resourceRegistry;

	public QuerySpecAdapterBuilder(QuerySpecDeserializer querySpecDeserializer, final ModuleRegistry moduleRegistry) {
		this.querySpecDeserializer = querySpecDeserializer;
		this.resourceRegistry = moduleRegistry.getResourceRegistry();
		this.querySpecDeserializer.init(new QuerySpecDeserializerContext() {

			@Override
			public ResourceRegistry getResourceRegistry() {
				return resourceRegistry;
			}

			@Override
			public TypeParser getTypeParser() {
				return moduleRegistry.getTypeParser();
			}
		});
	}

	@Override
	public QueryAdapter build(ResourceInformation resourceInformation, Map<String, Set<String>> parameters) {

		return new QuerySpecAdapter(querySpecDeserializer.deserialize(resourceInformation, parameters), resourceRegistry);
	}
}
