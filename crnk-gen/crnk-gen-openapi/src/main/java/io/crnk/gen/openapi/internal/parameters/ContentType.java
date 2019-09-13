package io.crnk.gen.openapi.internal.parameters;

import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;

import java.util.Arrays;

public class ContentType {
  public static Parameter parameter() {
    return new Parameter()
        .name("Content-Type")
        .in("header")
        .schema(
            new StringSchema()
                ._default("application/vnd.api+json")
                ._enum(Arrays.asList("application/vnd.api+json", "application/json")))
        .required(true);
  }
}
