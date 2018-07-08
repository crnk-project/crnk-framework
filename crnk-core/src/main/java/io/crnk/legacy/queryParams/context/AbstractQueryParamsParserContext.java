package io.crnk.legacy.queryParams.context;

import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.dispatcher.path.JsonPath;
import io.crnk.core.engine.registry.ResourceRegistry;

/**
 * @deprecated make use of QuerySpec
 */
@Deprecated
public abstract class AbstractQueryParamsParserContext implements QueryParamsParserContext {

	private final ResourceInformation resourceInformation;

	protected AbstractQueryParamsParserContext(ResourceRegistry resourceRegistry, JsonPath path) {
		resourceInformation = path.getRootEntry().getResourceInformation();
	}

	@Override
	public ResourceInformation getRequestedResourceInformation() {
		return resourceInformation;
	}
}
