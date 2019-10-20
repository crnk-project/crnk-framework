package io.crnk.gen.openapi.internal.schemas;

import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;

public class ResponseMixin extends AbstractSchemaGenerator {
  public Schema schema() {
    return new ObjectSchema()
        .description("A JSON:API document with a single resource")
        .addProperties(
            "errors",
            new ArraySchema().items(new ApiError().$ref()))
        .addProperties(
            "jsonapi",
            new ObjectSchema()
                .addProperties(
                    "version",
                    new StringSchema()))
        .addProperties(
            "links",
            new Schema().addProperties(
                "self",
                new StringSchema()
                    .description("the link that generated the current response document")))
        .addProperties(
            "included",
            new ArraySchema()
                .items(
                    new ObjectSchema()
                        .addProperties(
                            "type",
                            new StringSchema()
                                .description("The JSON:API resource type"))
                        .addProperties(
                            "id",
                            new StringSchema()
                                .description("The JSON:API resource ID")))
                .description("Included resources"));
  }
}
