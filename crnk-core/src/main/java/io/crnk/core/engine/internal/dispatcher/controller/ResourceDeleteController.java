package io.crnk.core.engine.internal.dispatcher.controller;

import io.crnk.core.engine.dispatcher.Response;
import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.document.Resource;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.information.resource.ResourceInformation;
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
import java.util.stream.Collectors;

public class ResourceDeleteController extends BaseController {

	@Override
	public boolean isAcceptable(JsonPath jsonPath, String method) {
		return jsonPath instanceof ResourcePath
				&& HttpMethod.DELETE.name().equals(method);
	}

	@Override
	public Result<Response> handleAsync(JsonPath jsonPath, QueryAdapter queryAdapter, Document requestDocument) {
		RegistryEntry registryEntry = jsonPath.getRootEntry();
		Collection<Serializable> ids = jsonPath.getIds();
		logger.debug("using registry entry {}", registryEntry);
		List<Result<JsonApiResponse>> results = new ArrayList<>();
		ResourceRepositoryAdapter resourceRepository = registryEntry.getResourceRepository();
		if (ids == null) {
			List<Resource> resourceBodies = getRequestBodys(requestDocument, jsonPath, HttpMethod.DELETE);
			ids = resourceBodies.stream()
					.map(resource -> getParsedId(resource, resourceRepository.getRepositoryInformation().getResource()))
					.collect(Collectors.toList());
			logger.debug("deleting ids={}", ids);
			Result<JsonApiResponse> result = resourceRepository.delete(ids, queryAdapter);
			results.add(result);
		} else {
			logger.debug("deleting ids={}", ids);
			ids.forEach(id -> results.add(resourceRepository.delete(id, queryAdapter)));
		}

		ResultFactory resultFactory = context.getResultFactory();
		int status = getStatus(null, HttpMethod.DELETE);
		Response response = new Response(null, status);
		if (results.isEmpty()) {
			return resultFactory.just(response);
		}
		return resultFactory.zip(results).map(it -> {
			logger.debug("set response {}", it);
			return response;
		});
	}

	protected Serializable getParsedId(Resource dataBody, ResourceInformation resourceInformation) {
		String id = dataBody.getId();
		return resourceInformation.parseIdString(id);
	}

}
