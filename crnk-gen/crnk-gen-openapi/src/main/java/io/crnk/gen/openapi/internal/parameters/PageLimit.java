package io.crnk.gen.openapi.internal.parameters;

import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.parameters.Parameter;

import java.math.BigDecimal;

public class PageLimit extends AbstractParameterGenerator {
  public Parameter parameter() {
    return new Parameter().name("page[limit]")
        .description("Max number of items")
        .in("query")
        .schema(
            new IntegerSchema()
                .format("int64")
                ._default(100)  // TODO: resolve from application.properties.crnk.default-page-limit=20
                .maximum(BigDecimal.valueOf(1000)));  // TODO: resolve from application.properties.crnk.max-page-limit=1000

  }
}
