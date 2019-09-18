package io.crnk.gen.openapi.internal.responses;

import io.crnk.meta.model.resource.MetaResource;
import io.swagger.v3.oas.models.responses.ApiResponse;


abstract class AbstractResourceResponseGenerator {
  protected final MetaResource metaResource;

  protected AbstractResourceResponseGenerator(MetaResource metaResource) {
    this.metaResource = metaResource;
  }

  public String getName() {
    return metaResource.getResourceType() + getClass().getSimpleName();
  }

  public ApiResponse $ref() {
    return new ApiResponse().$ref(getName());
  }
}
