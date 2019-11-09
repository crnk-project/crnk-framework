package io.crnk.gen.openapi.internal.schemas;

import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;

import java.util.Arrays;
import java.util.Collections;

public class Link extends AbstractSchemaGenerator {
  public Schema schema() {
    return new ComposedSchema()
        .oneOf(
            Arrays.asList(
                new StringSchema()
                    .description("A string containing the link's URL.")
                    .format("uri"),
                new ObjectSchema()
                    .required(Collections.singletonList("href"))
                    .addProperties(
                        "href",
                        new StringSchema()
                            .format("uri")
                            .description("A string containing the link's URL."))
                    .addProperties(
                        "meta",
                        new Meta().$ref())))
        .description("A link **MUST** be represented as either: a string containing the link's URL or a link object.");
  }
}
