package io.crnk.gen.openapi.internal.schemas;

import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;

public class Pagination extends AbstractSchemaGenerator {
  public Schema schema() {
    return new ObjectSchema()
        .addProperties(
            "first",
            new StringSchema()
                .description("The first page of data")
                .format("uri")
                .nullable(true))
        .addProperties(
            "last",
            new StringSchema()
                .description("The last page of data")
                .format("uri")
                .nullable(true))
        .addProperties(
            "prev",
            new StringSchema()
                .description("The previous page of data")
                .format("uri")
                .nullable(true))
        .addProperties(
            "next",
            new StringSchema()
                .description("The next page of data")
                .format("uri")
                .nullable(true));
  }
}
