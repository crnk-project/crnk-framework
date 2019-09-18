package io.crnk.gen.openapi.internal.parameters;

import io.crnk.meta.model.resource.MetaResource;
import io.swagger.v3.oas.models.parameters.Parameter;


abstract class AbstractResourceParameterGenerator {
  protected final MetaResource metaResource;

  protected AbstractResourceParameterGenerator(MetaResource metaResource) {
    this.metaResource = metaResource;
  }

  public String getName() {
    return metaResource.getResourceType() + getClass().getSimpleName();
  }

  public Parameter $ref() {
    return new Parameter().$ref(getName());
  }
}
