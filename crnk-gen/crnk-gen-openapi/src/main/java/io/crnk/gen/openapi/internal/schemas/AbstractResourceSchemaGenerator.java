package io.crnk.gen.openapi.internal.schemas;

import io.crnk.meta.model.resource.MetaResource;
import io.swagger.v3.oas.models.media.Schema;


abstract class AbstractResourceSchemaGenerator {
  protected final MetaResource metaResource;

  protected AbstractResourceSchemaGenerator(MetaResource metaResource) {
    this.metaResource = metaResource;
  }

  public String getName() {
    return metaResource.getResourceType() + getClass().getSimpleName();
  }

  public Schema $ref() {
    return new Schema().$ref(getName());
  }
}
