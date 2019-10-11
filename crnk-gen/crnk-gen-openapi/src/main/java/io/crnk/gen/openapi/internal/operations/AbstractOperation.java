package io.crnk.gen.openapi.internal.operations;

import io.crnk.gen.openapi.internal.OASErrors;
import io.crnk.gen.openapi.internal.OperationType;
import io.crnk.gen.openapi.internal.parameters.ContentType;
import io.crnk.gen.openapi.internal.responses.Accepted;
import io.crnk.gen.openapi.internal.responses.NoContent;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

abstract class AbstractOperation {

  public static OperationType operationType;

  Map<String, ApiResponse> defaultResponsesMap() {
    Map<String, ApiResponse> responses = new TreeMap<String, ApiResponse>() {
    };

    responses.put("202", new Accepted().$ref());
    responses.put("204", new NoContent().$ref());

    Map<String, ApiResponse> apiResponseCodes = OASErrors.generateStandardApiErrorResponses();

    for (Map.Entry<String, ApiResponse> entry : apiResponseCodes.entrySet()) {
      responses.put(entry.getKey(), new ApiResponse().$ref(entry.getKey()));
    }

    return responses;
  }

  abstract public boolean isEnabled();

  abstract public String getDescription();

  Operation operation() {
    return new Operation()
        .parameters(
            new ArrayList<>(
                Collections.singletonList(new ContentType().parameter())))
        .description(getDescription());
  }

  static ApiResponses apiResponsesFromMap(Map<String, ApiResponse> responseMap) {
    ApiResponses responses = new ApiResponses();
    responseMap.forEach(responses::addApiResponse);
    return responses;
  }
}
