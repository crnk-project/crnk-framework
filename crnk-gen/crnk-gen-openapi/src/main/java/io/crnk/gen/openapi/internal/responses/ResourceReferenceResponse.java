package io.crnk.gen.openapi.internal.responses;


import io.crnk.core.engine.http.HttpStatus;
import io.crnk.gen.openapi.internal.schemas.ResourceReferenceResponseSchema;
import io.crnk.meta.model.resource.MetaResource;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;

public class ResourceReferenceResponse extends AbstractResponseGenerator {

  public ResourceReferenceResponse(MetaResource metaResource) {
    super(metaResource);
  }

  public ApiResponse response() {
    return new ApiResponse()
        .description(HttpStatus.toMessage(200))
        .content(
            new Content()
                .addMediaType(
                    "application/vnd.api+json",
                    new MediaType()
                        .schema(new ResourceReferenceResponseSchema(metaResource).$ref())));
  }
}
