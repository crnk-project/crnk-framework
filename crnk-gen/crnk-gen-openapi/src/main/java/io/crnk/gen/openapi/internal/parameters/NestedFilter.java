package io.crnk.gen.openapi.internal.parameters;

import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.parameters.Parameter;

public class NestedFilter extends AbstractParameterGenerator {

  public Parameter parameter() {
    return new Parameter()
        .name("filter")
        .description("Customizable query (experimental)")
        .in("query")
        .schema(
            // TODO: This schema should be referenced recursively,
            //  but that breaks swagger-ui (https://github.com/swagger-api/swagger-ui/issues/3325)
            new ObjectSchema()
                .addProperties(
                    "AND",
                    new ObjectSchema()
                        .additionalProperties(true)
                        .nullable(true))
                .addProperties(
                    "OR",
                    new ObjectSchema()
                        .additionalProperties(true)
                        .nullable(true))
                .addProperties(
                    "NOT",
                    new ObjectSchema()
                        .additionalProperties(true)
                        .nullable(true))
                .additionalProperties(true));
  }
}
