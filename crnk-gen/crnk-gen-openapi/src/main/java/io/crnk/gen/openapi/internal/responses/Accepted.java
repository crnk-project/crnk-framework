package io.crnk.gen.openapi.internal.responses;


import io.crnk.gen.openapi.internal.schemas.Success;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;

public class Accepted extends AbstractResponseGenerator {
  public ApiResponse response() {
    return new ApiResponse()
        .description("Accepted")
        .content(new Content()
            .addMediaType("application/vnd.api+json",
                new MediaType()
                    .schema(new Success().$ref())));
  }
}
