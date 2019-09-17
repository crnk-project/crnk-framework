package io.crnk.gen.openapi.internal.responses;


import io.crnk.core.engine.http.HttpStatus;
import io.crnk.meta.model.resource.MetaResource;
import io.swagger.v3.oas.models.media.*;
import io.swagger.v3.oas.models.responses.ApiResponse;

public class RelationshipMultiResponse {
  MetaResource metaResource;

  public RelationshipMultiResponse(MetaResource metaResource) {
    this.metaResource = metaResource;
  }

  public ApiResponse response() {
    return new ApiResponse()
        .description(HttpStatus.toMessage(200))
        .content(
            new Content()
                .addMediaType(
                    "application/vnd.api+json",
                    new MediaType()
                        .schema(
                            new Schema()
                                .$ref(metaResource.getName() + "Relationships"))));
  }
}
