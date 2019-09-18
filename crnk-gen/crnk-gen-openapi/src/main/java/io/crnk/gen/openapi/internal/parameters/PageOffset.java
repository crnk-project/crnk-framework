package io.crnk.gen.openapi.internal.parameters;

import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.parameters.Parameter;

public class PageOffset extends AbstractStaticParameterGenerator {
  public static Parameter parameter() {
    return new Parameter().name("page[offset]")
        .description("Page offset")
        .in("query")
        .schema(
            new IntegerSchema()
                .format("int64")
                ._default(0));
  }
}
