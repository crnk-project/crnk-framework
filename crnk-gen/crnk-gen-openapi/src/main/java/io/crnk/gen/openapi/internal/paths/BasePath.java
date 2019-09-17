package io.crnk.gen.openapi.internal.paths;

import io.crnk.gen.openapi.internal.OASErrors;
import io.crnk.gen.openapi.internal.OASResource;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public class BasePath {
  OASResource oasResource;

  Operation generateDefaultOperation() {
    return new Operation().parameters(
        new ArrayList<>(
            Collections.singletonList(
                new Parameter()
                    .$ref("#/components/parameters/ContentType"))));
  }

  Operation generateDefaultGetRelationshipsOrFieldsOperation(OASResource relatedOasResource, boolean oneToMany) {
    Operation operation = generateDefaultOperation();
    operation.getParameters().add(new Parameter().$ref("#/components/parameters/" + oasResource.getResourceType() + "PrimaryKey"));

    // TODO: Pull these out into re-usable parameter groups when https://github.com/OAI/OpenAPI-Specification/issues/445 lands
    // Add filter[<>] parameters
    // Only the most basic filters are documented
    if (oneToMany) {
      oasResource.addFilters(operation);
    }
    // Add fields[resource] parameter
    operation.getParameters().add(new Parameter().$ref("#/components/parameters/" + relatedOasResource.getResourceType() + "Fields"));
    // Add include parameter
    operation.getParameters().add(new Parameter().$ref("#/components/parameters/" + relatedOasResource.getResourceType() + "Include"));

    return operation;
  }

  Map<String, ApiResponse> generateDefaultResponsesMap() {
    Map<String, ApiResponse> responses = new TreeMap<String, ApiResponse>() {
    };

    responses.put("202", new ApiResponse().$ref("202"));
    responses.put("204", new ApiResponse().$ref("204"));

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

  /*
 		Generate a sensible, default ApiResponses that is populated with references
 		to all Error Responses for a metaResource
 	 */
  static ApiResponses apiResponsesFromMap(Map<String, ApiResponse> responseMap) {
    ApiResponses responses = new ApiResponses();
    responseMap.forEach(responses::addApiResponse);
    return responses;
  }

  Operation generateDefaultRelationshipOperation(OASResource relatedOasResource, boolean oneToMany, boolean includeBody) {
    Operation operation = generateDefaultOperation();
    operation.getParameters().add(new Parameter().$ref("#/components/parameters/" + oasResource.getResourceType() + "PrimaryKey"));
    String postFix = oneToMany ? "Relationships" : "Relationship";
    if (!includeBody) {
      return operation;
    }
    operation.setRequestBody(
        new RequestBody()
            .content(
                new Content()
                    .addMediaType("application/vnd.api+json",
                        new MediaType()
                            .schema(
                                new Schema()
                                    .$ref(relatedOasResource.getResourceName() + postFix)))));
    return operation;
  }
}
