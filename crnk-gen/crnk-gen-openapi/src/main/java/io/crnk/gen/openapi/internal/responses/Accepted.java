package io.crnk.gen.openapi.internal.responses;


import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;

public class Accepted extends AbstractResponseGenerator {
  public ApiResponse response() {
    return new ApiResponse()
        .description("Accepted")
        .content(new Content()
            .addMediaType("application/vnd.api+json",
                new MediaType()
                    .schema(
                        new ObjectSchema()
                            .addProperties(
                                "id",
                                new StringSchema()
                                    .description("a unique identifier for this pending action")))));
  }
}
