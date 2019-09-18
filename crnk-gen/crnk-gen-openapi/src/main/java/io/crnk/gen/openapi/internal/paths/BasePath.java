package io.crnk.gen.openapi.internal.paths;

import io.crnk.gen.openapi.internal.OASErrors;
import io.crnk.gen.openapi.internal.OASResource;
import io.crnk.gen.openapi.internal.parameters.ContentType;
import io.crnk.gen.openapi.internal.parameters.Fields;
import io.crnk.gen.openapi.internal.parameters.Include;
import io.crnk.gen.openapi.internal.parameters.PrimaryKey;
import io.crnk.gen.openapi.internal.responses.Accepted;
import io.crnk.gen.openapi.internal.responses.NoContent;
import io.crnk.gen.openapi.internal.schemas.ResourceReferenceResponseSchema;
import io.crnk.gen.openapi.internal.schemas.ResourceReferencesResponseSchema;
import io.crnk.meta.model.resource.MetaResource;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public class BasePath {
  MetaResource metaResource;

  /*
 		Generate a sensible, default ApiResponses that is populated with references
 		to all Error Responses for a metaResource
 	 */
  static ApiResponses apiResponsesFromMap(Map<String, ApiResponse> responseMap) {
    ApiResponses responses = new ApiResponses();
    responseMap.forEach(responses::addApiResponse);
    return responses;
  }

  Operation generateDefaultOperation() {
    return new Operation().parameters(
        new ArrayList<>(
            Collections.singletonList(ContentType.parameter())));
  }

  Operation generateDefaultGetRelationshipsOrFieldsOperation(MetaResource relatedMetaResource, boolean oneToMany) {
    Operation operation = generateDefaultOperation();
    operation.getParameters().add(new PrimaryKey(metaResource).$ref());

    // TODO: Pull these out into re-usable parameter groups when https://github.com/OAI/OpenAPI-Specification/issues/445 lands
    // Add filter[<>] parameters
    // Only the most basic filters are documented
    if (oneToMany) {
      OASResource.addFilters(metaResource, operation);
    }
    // Add fields[resource] parameter
    operation.getParameters().add(new Fields(metaResource).$ref());
    // Add include parameter
    operation.getParameters().add(new Include(metaResource).$ref());

    return operation;
  }

  Map<String, ApiResponse> generateDefaultResponsesMap() {
    Map<String, ApiResponse> responses = new TreeMap<String, ApiResponse>() {
    };

    responses.put("202", new Accepted().$ref());
    responses.put("204", new NoContent().$ref());

    Map<String, ApiResponse> apiResponseCodes = OASErrors.generateStandardApiErrorResponses();
    for (Map.Entry<String, ApiResponse> entry : apiResponseCodes.entrySet()) {

      // TODO: Check to see (somehow) if the metaResource returns this response code
      // Add reference to error response stored in #/components/responses/<HttpCode>
      responses.put(entry.getKey(), new ApiResponse().$ref(entry.getKey()));
    }

    // Todo: Standard wrapper responses for single & multiple records
    // responses...

    return responses;
  }

  Operation generateDefaultRelationshipOperation(MetaResource relatedMetaResource, boolean oneToMany, boolean includeBody) {
    Operation operation = generateDefaultOperation();
    operation.getParameters().add(new PrimaryKey(metaResource).$ref());
    Schema relationshipSchema = oneToMany ? new ResourceReferencesResponseSchema(metaResource).$ref() : new ResourceReferenceResponseSchema(metaResource).$ref();
    if (!includeBody) {
      return operation;
    }
    operation.setRequestBody(
        new RequestBody()
            .content(
                new Content()
                    .addMediaType("application/vnd.api+json",
                        new MediaType()
                            .schema(relationshipSchema))));
    return operation;
  }
}
