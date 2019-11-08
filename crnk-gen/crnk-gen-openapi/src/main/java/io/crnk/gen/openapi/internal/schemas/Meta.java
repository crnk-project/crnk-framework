package io.crnk.gen.openapi.internal.schemas;

import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;

public class Meta extends AbstractSchemaGenerator {
  public Schema schema() {
    return new ObjectSchema()
        .description("Non-standard meta-information that can not be represented as an attribute or relationship.")
        .additionalProperties(true);
  }
}
