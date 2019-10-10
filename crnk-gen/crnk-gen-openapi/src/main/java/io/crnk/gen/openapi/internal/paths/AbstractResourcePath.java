package io.crnk.gen.openapi.internal.paths;

import io.crnk.gen.openapi.internal.OASErrors;
import io.crnk.gen.openapi.internal.parameters.ContentType;
import io.crnk.gen.openapi.internal.responses.Accepted;
import io.crnk.gen.openapi.internal.responses.NoContent;
import io.crnk.meta.model.resource.MetaResource;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

abstract class AbstractResourcePath {

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
            Collections.singletonList(new ContentType().parameter())));
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
}
