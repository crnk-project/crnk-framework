package io.crnk.gen.openapi.internal.schemas;

import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Schema;

public class ListResponseMixin extends AbstractStaticSchemaGenerator {
  public static Schema schema() {
    return new Schema()
        .type("object")
        .description("A page of results")
        .addProperties(
            "jsonapi",
            new Schema()
                .type("object")
                .addProperties(
                    "version",
                    new Schema().type("string")))
        .addProperties(
            "errors",
            new ArraySchema().items(ApiError.$ref()))
        .addProperties(
            "meta",
            new Schema()
                .type("object")
                // TODO: Determine if this is supported
                //								.addProperties(
                //										"total-pages",
                //										new Schema()
                //												.type("integer")
                //												.description("The total number of pages available"))
                .addProperties(
                    "totalResourceCount",
                    new Schema()
                        .type("integer")
                        .description("The total number of items available"))
                .additionalProperties(true))
        .addProperties(
            "links",
            new Schema()
                .type("object")
                .addProperties(
                    "self",
                    new Schema()
                        .type("string")
                        .description("Link to this page of results"))
                .addProperties(
                    "prev",
                    new Schema()
                        .type("string")
                        .description("Link to the previous page of results"))
                .addProperties(
                    "next",
                    new Schema()
                        .type("string")
                        .description("Link to the next page of results"))
                .addProperties(
                    "last",
                    new Schema()
                        .type("string")
                        .description("Link to the last page of results"))
                .addProperties(
                    "first",
                    new Schema()
                        .type("string")
                        .description("Link to the first page of results")));
  }
}
