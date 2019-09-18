package io.crnk.gen.openapi.internal.responses;


import io.crnk.core.engine.http.HttpStatus;
import io.crnk.gen.openapi.internal.schemas.ResourcesResponseSchema;
import io.crnk.meta.model.resource.MetaResource;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;

public class ResourcesResponse extends AbstractResourceResponseGenerator {

  public ResourcesResponse(MetaResource metaResource) {
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
                        .schema(new ResourcesResponseSchema(metaResource).$ref())));
  }
}
