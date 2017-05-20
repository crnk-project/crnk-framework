package io.crnk.core.engine.filter;

import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.internal.dispatcher.path.JsonPath;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.legacy.internal.RepositoryMethodParameterProvider;
import io.crnk.legacy.queryParams.QueryParams;

/**
 * Provides request information to {@link DocumentFilter}.
 */
public interface DocumentFilterContext {

	Document getRequestBody();

	RepositoryMethodParameterProvider getParameterProvider();

	QueryParams getQueryParams();

	JsonPath getJsonPath();

	QueryAdapter getQueryAdapter();

	String getMethod();

}
