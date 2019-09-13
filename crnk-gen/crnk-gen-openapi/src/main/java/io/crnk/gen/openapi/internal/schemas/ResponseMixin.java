package io.crnk.gen.openapi.internal.schemas;

import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Schema;

public class ResponseMixin {
  public static Schema schema() {
    return new Schema()
        .type("object")
        .description("A JSON-API document with a single resource")
        .addProperties(
            "errors",
            new ArraySchema().items(new Schema().$ref("ApiError")))
        .addProperties(
            "jsonapi",
            new Schema()
                .type("object")
                .addProperties(
                    "version",
                    new Schema().type("string")))
        .addProperties(
            "links",
            new Schema().addProperties(
                "self",
                new Schema()
                    .type("string")
                    .description("the link that generated the current response document")))
        .addProperties(
            "included",
            new ArraySchema()
                .items(
                    new Schema()
                        .type("object")
                        .addProperties(
                            "type",
                            new Schema()
                                .type("string")
                                .description("The JSON:API resource type"))
                        .addProperties(
                            "id",
                            new Schema()
                                .type("string")
                                .description("The JSON:API resource ID")))
                .description("Included resources"));
  }
}
