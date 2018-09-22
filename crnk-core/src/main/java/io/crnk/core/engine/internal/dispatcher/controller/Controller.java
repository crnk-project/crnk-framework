package io.crnk.core.engine.internal.dispatcher.controller;

import io.crnk.core.engine.dispatcher.Response;
import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.internal.dispatcher.path.JsonPath;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.result.Result;

public interface Controller {

	void init(ControllerContext context);

	/**
	 * Checks if requested repository method is acceptable.
	 *
	 * @param jsonPath Requested resource path
	 * @param method   HTTP request type
	 * @return Acceptance result in boolean
	 */
	boolean isAcceptable(JsonPath jsonPath, String method);

	@Deprecated
	Response handle(JsonPath jsonPath, QueryAdapter queryAdapter, Document requestDocument);

	Result<Response> handleAsync(JsonPath jsonPath, QueryAdapter queryAdapter, Document requestDocument);
}
