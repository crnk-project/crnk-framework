package io.crnk.core.engine.internal.dispatcher.controller;

import io.crnk.core.engine.dispatcher.Response;
import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.internal.dispatcher.path.JsonPath;
import io.crnk.core.engine.internal.dispatcher.path.RelationshipsPath;
import io.crnk.core.engine.internal.document.mapper.DocumentMapper;
import io.crnk.core.engine.internal.document.mapper.DocumentMappingConfig;
import io.crnk.core.engine.internal.repository.RelationshipRepositoryAdapter;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.result.Result;
import io.crnk.core.repository.response.JsonApiResponse;
import io.crnk.core.utils.Nullable;
import io.crnk.legacy.internal.RepositoryMethodParameterProvider;

import java.io.Serializable;

public class RelationshipsResourceGetController extends ResourceIncludeField {

	@Override
	public boolean isAcceptable(JsonPath jsonPath, String method) {
		return !jsonPath.isCollection() && jsonPath instanceof RelationshipsPath && HttpMethod.GET.name().equals(method);
	}

	@Override
	public Result<Response> handleAsync(JsonPath jsonPath, QueryAdapter queryAdapter, RepositoryMethodParameterProvider parameterProvider, Document requestBody) {
		RelationshipsPath relationshipsPath = (RelationshipsPath) jsonPath;
		RegistryEntry registryEntry = relationshipsPath.getRootEntry();

		Serializable id = jsonPath.getId();
		ResourceField relationshipField = relationshipsPath.getRelationship();

		DocumentMappingConfig documentMapperConfig = DocumentMappingConfig.create().setParameterProvider(parameterProvider);
		DocumentMapper documentMapper = context.getDocumentMapper();

		boolean isCollection = Iterable.class.isAssignableFrom(relationshipField.getType());
		RelationshipRepositoryAdapter relationshipRepositoryForClass = registryEntry.getRelationshipRepository(relationshipField, parameterProvider);
		Result<JsonApiResponse> response;
		if (isCollection) {
			response = relationshipRepositoryForClass.findManyTargets(id, relationshipField, queryAdapter);
		} else {
			response = relationshipRepositoryForClass.findOneTarget(id, relationshipField, queryAdapter);
		}
		return response.merge(it -> documentMapper.toDocument(it, queryAdapter, documentMapperConfig)).map(this::toResponse);
	}


	public Response toResponse(Document document) {
		// return explicit { data : null } if values found
		if (!document.getData().isPresent()) {
			document.setData(Nullable.nullValue());
		}
		return new Response(document, 200);
	}

}
