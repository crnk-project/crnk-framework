package io.crnk.core.engine.internal.dispatcher.controller;

import io.crnk.core.engine.dispatcher.Response;
import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.internal.dispatcher.path.JsonPath;
import io.crnk.core.engine.internal.dispatcher.path.ResourcePath;
import io.crnk.core.engine.internal.repository.ResourceRepositoryAdapter;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.result.Result;
import io.crnk.core.engine.result.ResultFactory;
import io.crnk.core.repository.response.JsonApiResponse;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ResourceDeleteController extends BaseController {

	@Override
	public boolean isAcceptable(JsonPath jsonPath, String method) {
		return !jsonPath.isCollection()
				&& jsonPath instanceof ResourcePath
				&& HttpMethod.DELETE.name().equals(method);
	}

	@Override
	public Result<Response> handleAsync(JsonPath jsonPath, QueryAdapter queryAdapter, Document requestBody) {
		RegistryEntry registryEntry = jsonPath.getRootEntry();
		Collection<Serializable> ids = jsonPath.getIds();
		logger.debug("using registry entry {}", registryEntry);
		logger.debug("deleting ids={}", ids);

		List<Result<JsonApiResponse>> results = new ArrayList<>();
		for (Serializable id : ids) {
			ResourceRepositoryAdapter resourceRepository = registryEntry.getResourceRepository();
			Result<JsonApiResponse> result = resourceRepository.delete(id, queryAdapter);
			results.add(result);
		}

		ResultFactory resultFactory = context.getResultFactory();
		Response response = new Response(null, 204);
		if (results.isEmpty()) {
			return resultFactory.just(response);
		}
		return resultFactory.zip(results).map(it -> {
			logger.debug("set response {}", it);
			return response;
		});
	}
}
