package io.crnk.gen.openapi.internal.responses;

import io.swagger.v3.oas.models.responses.ApiResponse;

public class NoContent {
  public static ApiResponse response() {
    return new ApiResponse()
        .description("No Content");
  }
}
