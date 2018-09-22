package io.crnk.core.engine.filter;

import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.internal.dispatcher.path.JsonPath;
import io.crnk.core.engine.query.QueryAdapter;

/**
 * Provides request information to {@link DocumentFilter}.
 */
public interface DocumentFilterContext {

	Document getRequestBody();

	JsonPath getJsonPath();

	QueryAdapter getQueryAdapter();

	String getMethod();

}
