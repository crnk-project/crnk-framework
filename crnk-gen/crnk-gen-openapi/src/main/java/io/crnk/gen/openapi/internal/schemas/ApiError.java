package io.crnk.gen.openapi.internal.schemas;

import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;

public class ApiError extends AbstractSchemaGenerator {
  public Schema schema() {
    return new ObjectSchema()
        .addProperties(
            "id",
            new StringSchema()
                .description("a unique identifier for this particular occurrence of the problem"))
        .addProperties("links",
            new ObjectSchema()
                .addProperties(
                    "about",
                    new StringSchema()
                        .description("a link that leads to further details about this particular occurrence of the problem")))
        .addProperties(
            "status",
            new StringSchema()
                .description("the HTTP status code applicable to this problem, expressed as a string value"))
        .addProperties(
            "code",
            new StringSchema()
                .description("an application-specific error code, expressed as a string value"))
        .addProperties(
            "title",
            new StringSchema()
                .description("a short, human-readable summary of the problem that SHOULD NOT change from occurrence to occurrence of the problem, except for purposes of localization"))
        .addProperties(
            "detail",
            new StringSchema()
                .description("a human-readable explanation specific to this occurrence of the problem. Like 'title', this field’s value can be localized."))
        .addProperties(
            "source",
            new ObjectSchema()
                .addProperties(
                    "pointer",
                    new StringSchema()
                        .description("a JSON Pointer [RFC6901] to the associated entity in the request document"))
                .addProperties(
                    "parameter",
                    new StringSchema()
                        .description("a string indicating which URI query parameter caused the error")))
        .addProperties(
            "meta",
            new Meta().$ref())
        .additionalProperties(false);
  }
}
