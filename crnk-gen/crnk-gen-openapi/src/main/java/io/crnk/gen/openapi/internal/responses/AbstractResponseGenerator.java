package io.crnk.gen.openapi.internal.responses;

import io.crnk.meta.model.resource.MetaResource;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.apache.commons.lang3.StringUtils;


abstract class AbstractResponseGenerator {

  protected final MetaResource metaResource;

  private final String prefix;

  AbstractResponseGenerator() {
    this.metaResource = null;
    prefix = "";
  }

  AbstractResponseGenerator(MetaResource metaResource) {
    this.metaResource = metaResource;
    prefix = StringUtils.capitalize(metaResource.getResourceType());
  }

  public String getName() {
    return prefix + getClass().getSimpleName();
  }

  public ApiResponse $ref() {
    return new ApiResponse().$ref(getName());
  }

  abstract public ApiResponse response();
}
