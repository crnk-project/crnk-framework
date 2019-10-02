package io.crnk.gen.openapi.internal.schemas;

import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;

public class ListResponseMixin extends AbstractSchemaGenerator {
  public Schema schema() {
    return new ObjectSchema()
        .description("A page of results")
        .addProperties(
            "jsonapi",
            new ObjectSchema()
                .addProperties(
                    "version",
                    new StringSchema()))
        .addProperties(
            "errors",
            new ArraySchema().items(new ApiError().$ref()))
        .addProperties(
            "meta",
            new ObjectSchema()
                .addProperties(
                    "totalResourceCount",
                    new IntegerSchema()
                        .description("The total number of items available"))
                .additionalProperties(true))
        .addProperties(
            "links",
            new ObjectSchema()
                .addProperties(
                    "self",
                    new StringSchema()
                        .description("Link to this page of results"))
                .addProperties(
                    "prev",
                    new StringSchema()
                        .description("Link to the previous page of results"))
                .addProperties(
                    "next",
                    new StringSchema()
                        .description("Link to the next page of results"))
                .addProperties(
                    "last",
                    new StringSchema()
                        .description("Link to the last page of results"))
                .addProperties(
                    "first",
                    new StringSchema()
                        .description("Link to the first page of results")));
  }
}
