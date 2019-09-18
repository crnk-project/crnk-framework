package io.crnk.gen.openapi.internal;

import io.crnk.core.engine.http.HttpStatus;
import io.crnk.gen.openapi.internal.schemas.ApiError;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class OASErrors {
  public static Map<String, ApiResponse> generateStandardApiErrorResponses() {
    Map<String, ApiResponse> responses = new LinkedHashMap<>();

    List<Integer> responseCodes = getStandardHttpStatusCodes();
    for (Integer responseCode : responseCodes) {
      if (responseCode >= 400 && responseCode <= 599) {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.description(HttpStatus.toMessage(responseCode));
        apiResponse.content(new Content()
            .addMediaType("application/json",
                new MediaType().schema(ApiError.$ref()))
        );
        responses.put(responseCode.toString(), apiResponse);
      }
    }

    return responses;
  }

  /*
 		Crnk maintains a list of HTTP status codes in io.crnk.core.engine.http.HttpStatus
 		as static fields. Iterate through and collect them into a list for use elsewhere.
 	 */
  private static List<Integer> getStandardHttpStatusCodes() {
    List<Integer> responseCodes = new ArrayList<>();

    Field[] fields = HttpStatus.class.getDeclaredFields();
    for (Field f : fields) {
      if (Modifier.isStatic(f.getModifiers())) {
        try {
          responseCodes.add(f.getInt(null));
        } catch (IllegalAccessException ignore) {
        }
      }
    }
    return responseCodes;
  }
}
