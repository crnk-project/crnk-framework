package io.crnk.gen.openapi.internal.schemas;

import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;

public class JsonApi extends AbstractSchemaGenerator {
  public Schema schema() {
    return new ObjectSchema()
        .addProperties(
            "version",
            new StringSchema())
        .additionalProperties(false);
  }
}
