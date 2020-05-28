package io.crnk.gen.openapi.internal.schemas;

import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;

import java.util.Collections;

public class Success extends AbstractSchemaGenerator {
  public Schema schema() {
    return new ObjectSchema()
        .description("A JSON:API document with a single resource")
        .addProperties("jsonapi", new JsonApi().$ref())
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
                                .description("The JSON:API resource ID"))
                        .addProperties(
                            "attributes",
                            new ObjectSchema()
                                .additionalProperties(true)))
                .uniqueItems(true)
                .description("Included resources"))
        .addProperties(
            "meta",
            new Meta().$ref())
        .addProperties(
            "links",
            new Links().$ref())
        .required(Collections.singletonList("data"));
  }
}
