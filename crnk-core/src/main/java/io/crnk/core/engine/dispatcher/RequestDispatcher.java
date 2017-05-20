package io.crnk.core.engine.dispatcher;

import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.http.HttpRequestContextBase;
import io.crnk.legacy.internal.RepositoryMethodParameterProvider;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

public interface RequestDispatcher {

	void process(HttpRequestContextBase requestContextBase) throws IOException;

	Response dispatchRequest(String jsonPath, String method, Map<String, Set<String>> parameters,
							 RepositoryMethodParameterProvider parameterProvider,
							 Document requestBody);

	void dispatchAction(String jsonPath, String method, Map<String, Set<String>> parameters);
}
