package io.crnk.gen.openapi.internal.schemas;

import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;

import java.util.Collections;

public class Info extends AbstractSchemaGenerator {
  public Schema schema() {
    return new ObjectSchema()
        .addProperties("jsonapi", new JsonApi().$ref())
        .addProperties(
            "meta",
            new Meta().$ref())
        .addProperties(
            "links",
            new Links().$ref())
        .required(Collections.singletonList("meta"))
        .additionalProperties(false);
  }
}
