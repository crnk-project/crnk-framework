package io.crnk.gen.openapi.internal.responses;

import io.crnk.meta.model.resource.MetaResource;
import io.swagger.v3.oas.models.responses.ApiResponse;


abstract class AbstractResponseGenerator {
  protected final MetaResource metaResource;
  protected final String prefix;

  protected AbstractResponseGenerator() {
    this.metaResource = null;
    prefix = "";
  }

  protected AbstractResponseGenerator(MetaResource metaResource) {
    this.metaResource = metaResource;
    prefix = metaResource.getResourceType();
  }

  public String getName() {
    return prefix + getClass().getSimpleName();
  }

  public ApiResponse $ref() {
    return new ApiResponse().$ref(getName());
  }

  abstract public ApiResponse response();
}
