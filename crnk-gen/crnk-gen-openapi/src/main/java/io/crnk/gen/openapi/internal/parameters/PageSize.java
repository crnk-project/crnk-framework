package io.crnk.gen.openapi.internal.parameters;

import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.parameters.Parameter;

public class PageSize extends AbstractParameterGenerator {
  public Parameter parameter() {
    return new Parameter().name("page[size]")
        .description("Page size")
        .in("query")
        .schema(
            new IntegerSchema()
                .format("int64")
                ._default(0)); // TODO: resolve from application.properties.crnk.default-page-limit=20
  }
}
