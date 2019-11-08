package io.crnk.gen.openapi.internal.responses;

import io.crnk.gen.openapi.internal.OASErrors;
import io.crnk.gen.openapi.internal.OASMergeUtil;
import io.swagger.v3.oas.models.responses.ApiResponse;

import java.util.LinkedHashMap;
import java.util.Map;

public class StaticResponses {
  public static Map<String, ApiResponse> generateStandardApiResponses() {
    return OASMergeUtil.mergeApiResponses(generateStandardApiSuccessResponses(), OASErrors.generateStandardApiErrorResponses());
  }

  private static Map<String, ApiResponse> generateStandardApiSuccessResponses() {
    Map<String, ApiResponse> responses = new LinkedHashMap<>();
    responses.put(new NoContent().getName(), new NoContent().response());

    return responses;
  }
}
