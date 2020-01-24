package io.crnk.reactive.internal.adapter;

import io.crnk.core.engine.dispatcher.RepositoryRequestSpec;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.internal.repository.RepositoryAdapterUtils;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.result.Result;
import io.crnk.core.engine.result.ResultFactory;
import io.crnk.core.module.ModuleRegistry;
import io.crnk.core.repository.response.JsonApiResponse;
import io.crnk.core.resource.links.LinksInformation;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.reactive.internal.MonoResult;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class ReactiveRepositoryAdapterBase {

	protected final ModuleRegistry moduleRegistry;

	protected final ResultFactory resultFactory;


	public ReactiveRepositoryAdapterBase(ModuleRegistry moduleRegistry) {
		this.moduleRegistry = moduleRegistry;
		this.resultFactory = this.moduleRegistry.getResultFactory();
	}


	protected Result<Map<Object, JsonApiResponse>> toResponses(Mono<Map> result, boolean isMany, QueryAdapter queryAdapter,
															   ResourceField field, HttpMethod method, RepositoryRequestSpec requestSpec) {

		Mono<Map<Object, JsonApiResponse>> mono = result.map((Map it) -> {
			Map<Object, JsonApiResponse> responses = new HashMap<>();
			Set<Map.Entry> entrySet = it.entrySet();
			for (Map.Entry entry : entrySet) {
				JsonApiResponse response = new JsonApiResponse();
				response.setEntity(entry.getValue());
				setInformation(response, requestSpec);
				responses.put(entry.getKey(), response);
			}
			return responses;
		});
		return new MonoResult<>(mono);
	}

	protected JsonApiResponse toSingleResult(Map<Object, JsonApiResponse> responseMap) {
		if (responseMap.size() > 1) {
			throw new IllegalStateException();
		}
		if (responseMap.isEmpty()) {
			return new JsonApiResponse();
		}
		return responseMap.values().iterator().next();
	}

	protected Result<JsonApiResponse> toResponse(Mono result, RepositoryRequestSpec requestSpec) {
		return new MonoResult(result)
				.map(it -> new JsonApiResponse().setEntity(it))
				.doWork(it -> setInformation((JsonApiResponse) it, requestSpec));
	}

	protected void setInformation(JsonApiResponse response, RepositoryRequestSpec requestSpec) {
		if (response.getEntity() instanceof ResourceList) {
			ResourceList list = (ResourceList) response.getEntity();
			response.setLinksInformation(list.getLinks());
			response.setMetaInformation(list.getMeta());
			// TODO reactive links/meta repository
		}

		Object entity = response.getEntity();
		LinksInformation linksInformation = response.getLinksInformation();
		linksInformation = RepositoryAdapterUtils.enrichLinksInformation(moduleRegistry, linksInformation, entity, requestSpec);
		response.setLinksInformation(linksInformation);
	}
}
