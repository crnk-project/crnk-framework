package io.crnk.gen.openapi.internal.responses;

import io.swagger.v3.oas.models.responses.ApiResponse;

abstract class AbstractStaticResponseGenerator {
  public String getName() {
    return getClass().getSimpleName();
  }

  public ApiResponse $ref() {
    return new ApiResponse().$ref(getName());
  }
}
