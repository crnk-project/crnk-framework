package io.crnk.core.engine.internal.dispatcher.controller;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import io.crnk.core.engine.dispatcher.Response;
import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.document.Resource;
import io.crnk.core.engine.document.ResourceIdentifier;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.dispatcher.path.JsonPath;
import io.crnk.core.engine.internal.dispatcher.path.RelationshipsPath;
import io.crnk.core.engine.internal.document.mapper.DocumentMapper;
import io.crnk.core.engine.internal.document.mapper.DocumentMappingConfig;
import io.crnk.core.engine.internal.repository.RelationshipRepositoryAdapter;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.result.Result;
import io.crnk.core.exception.RepositoryNotFoundException;
import io.crnk.core.queryspec.IncludeFieldSpec;
import io.crnk.core.queryspec.PathSpec;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.response.JsonApiResponse;
import io.crnk.core.utils.Nullable;

public class RelationshipsResourceGetController extends ResourceIncludeField {

	@Override
	public boolean isAcceptable(JsonPath jsonPath, String method) {
		return !jsonPath.isCollection() && jsonPath instanceof RelationshipsPath && HttpMethod.GET.name().equals(method);
	}

	@Override
	public Result<Response> handleAsync(JsonPath jsonPath, QueryAdapter queryAdapter, Document requestBody) {
		RelationshipsPath relationshipsPath = (RelationshipsPath) jsonPath;
		RegistryEntry rootEntry = relationshipsPath.getRootEntry();

		Serializable id = jsonPath.getId();
		ResourceField relationshipField = relationshipsPath.getRelationship();

		DocumentMappingConfig mappingConfig = context.getMappingConfig();
		DocumentMapper documentMapper = context.getDocumentMapper();

		// only the IDs necssary, update QuerySpec accordingly
		RegistryEntry relatedEntry = context.getResourceRegistry().getEntry(relationshipField.getOppositeResourceType());
		ResourceInformation relatedResourceInformation;
		if (relatedEntry != null) {
			relatedResourceInformation = relatedEntry.getResourceInformation();
			ResourceField idField = relatedResourceInformation.getIdField();
			QuerySpec querySpec = queryAdapter.toQuerySpec();
			querySpec.setIncludedFields(Arrays.asList(new IncludeFieldSpec(PathSpec.of(idField.getUnderlyingName()))));
		} else {
			relatedResourceInformation = null;
		}

		boolean isCollection = Iterable.class.isAssignableFrom(relationshipField.getType());
		RelationshipRepositoryAdapter relationshipRepositoryForClass = rootEntry.getRelationshipRepository(relationshipField);
		Result<JsonApiResponse> response;
		if (isCollection) {
			response = relationshipRepositoryForClass.findManyRelations(id, relationshipField, queryAdapter);
		} else {
			response = relationshipRepositoryForClass.findOneRelations(id, relationshipField, queryAdapter);
		}
		return response.merge(it -> documentMapper.toDocument(it, queryAdapter, mappingConfig)).map(it -> toResponse(it, relatedResourceInformation));
	}


	public Response toResponse(Document document, ResourceInformation resourceInformation) {
		// return explicit { data : null } if values found
		Nullable<Object> data = document.getData();
		if (data.isPresent()) {
			if (data.get() != null) {
				if (data.get() instanceof Collection) {
					Collection<Object> resources = (Collection) data.get();
					List<ResourceIdentifier> resourceIds = resources.stream()
							.map(it -> toResourceIdentifier(it, resourceInformation))
							.collect(Collectors.toList());
					document.setData(Nullable.of(resourceIds));
				} else {
					Object resource = data.get();
					document.setData(Nullable.of(toResourceIdentifier(resource, resourceInformation)));
				}
			}
		} else {
			document.setData(Nullable.nullValue());
		}
		return new Response(document, 200);
	}

	private ResourceIdentifier toResourceIdentifier(Object it, ResourceInformation resourceInformation) {
		if (it instanceof Resource) {
			return ((Resource) it).toIdentifier();
		}
		if (resourceInformation == null) {
			RegistryEntry entry = context.getResourceRegistry().findEntry(it.getClass());
			if (entry == null) {
				throw new RepositoryNotFoundException("no repository found for " + it);
			}
			resourceInformation = entry.getResourceInformation();
		}
		return resourceInformation.toResourceIdentifier(it);
	}

}
