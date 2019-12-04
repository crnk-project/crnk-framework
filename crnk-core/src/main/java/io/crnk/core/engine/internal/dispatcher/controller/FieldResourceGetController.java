package io.crnk.core.engine.internal.dispatcher.controller;

import java.io.Serializable;

import io.crnk.core.engine.dispatcher.Response;
import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.internal.dispatcher.path.FieldPath;
import io.crnk.core.engine.internal.dispatcher.path.JsonPath;
import io.crnk.core.engine.internal.document.mapper.DocumentMapper;
import io.crnk.core.engine.internal.document.mapper.DocumentMappingConfig;
import io.crnk.core.engine.internal.repository.RelationshipRepositoryAdapter;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.result.Result;
import io.crnk.core.repository.response.JsonApiResponse;
import io.crnk.core.utils.Nullable;

public class FieldResourceGetController extends ResourceIncludeField {

	@Override
	public boolean isAcceptable(JsonPath jsonPath, String method) {
		return !jsonPath.isCollection()
				&& FieldPath.class.equals(jsonPath.getClass())
				&& HttpMethod.GET.name().equals(method);
	}

	@Override
	public Result<Response> handleAsync(JsonPath jsonPath, QueryAdapter queryAdapter, Document requestBody) {
		FieldPath fieldPath = (FieldPath) jsonPath;

		RegistryEntry registryEntry = fieldPath.getRootEntry();
		logger.debug("using registry entry {}", registryEntry);
		Serializable castedResourceId = jsonPath.getId();
		ResourceField relationshipField = fieldPath.getField();

		// TODO remove Class usage and replace by resourceId
		Class<?> baseRelationshipFieldClass = relationshipField.getType();

		DocumentMappingConfig mappingConfig = context.getMappingConfig();
		DocumentMapper documentMapper = context.getDocumentMapper();

		RelationshipRepositoryAdapter relationshipRepositoryForClass = registryEntry.getRelationshipRepository(relationshipField);
		Result<JsonApiResponse> response;
		if (Iterable.class.isAssignableFrom(baseRelationshipFieldClass)) {
			response = relationshipRepositoryForClass.findManyRelations(castedResourceId, relationshipField, queryAdapter);
		} else {
			response = relationshipRepositoryForClass.findOneRelations(castedResourceId, relationshipField, queryAdapter);
		}
		return response.merge(it -> documentMapper.toDocument(it, queryAdapter, mappingConfig)).map(this::toResponse);
	}


	public Response toResponse(Document document) {
		// return explicit { data : null } if values found
		if (!document.getData().isPresent()) {
			document.setData(Nullable.nullValue());
		}
		int status = getStatus(document, HttpMethod.GET);
		return new Response(document, status);
	}
}
