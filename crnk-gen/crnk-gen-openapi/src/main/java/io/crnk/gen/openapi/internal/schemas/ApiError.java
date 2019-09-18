package io.crnk.gen.openapi.internal.schemas;

import io.swagger.v3.oas.models.media.Schema;

public class ApiError extends AbstractStaticSchemaGenerator {
  public static Schema schema() {
    return new Schema()
        .type("object")
        .addProperties(
            "id",
            new Schema()
                .type("string")
                .description("a unique identifier for this particular occurrence of the problem"))
        .addProperties("links",
            new Schema()
                .type("object")
                .addProperties(
                    "about",
                    new Schema()
                        .type("string")
                        .description("a link that leads to further details about this particular occurrence of the problem")))
        .addProperties(
            "status",
            new Schema()
                .type("string")
                .description("the HTTP status code applicable to this problem, expressed as a string value"))
        .addProperties(
            "code",
            new Schema()
                .type("string")
                .description("an application-specific error code, expressed as a string value"))
        .addProperties(
            "title",
            new Schema()
                .type("string")
                .description("a short, human-readable summary of the problem that SHOULD NOT change from occurrence to occurrence of the problem, except for purposes of localization"))
        .addProperties(
            "detail",
            new Schema()
                .type("string")
                .description("a human-readable explanation specific to this occurrence of the problem. Like 'title', this field’s value can be localized."))
        .addProperties(
            "source",
            new Schema()
                .type("object")
                .addProperties(
                    "pointer",
                    new Schema()
                        .type("string")
                        .description("a JSON Pointer [RFC6901] to the associated entity in the request document"))
                .addProperties(
                    "parameter",
                    new Schema()
                        .type("string")
                        .description("a string indicating which URI query parameter caused the error")))
        .addProperties(
            "meta",
            new Schema()
                .additionalProperties(true)
                .description("a meta object containing non-standard meta-information about the error"));
  }
}
