package io.crnk.gen.openapi.internal.schemas;

import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;

import java.util.Arrays;

public class Links extends AbstractSchemaGenerator {
  public Schema schema() {
    return new ComposedSchema()
        .allOf(
            Arrays.asList(
                new ObjectSchema()
                    .additionalProperties(
                        new Link().$ref()),
                new Pagination().$ref()
            ))
        .description("Link members related to the primary data.");
  }
}
