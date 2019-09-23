package io.crnk.gen.openapi.internal.schemas;

import io.crnk.meta.model.resource.MetaResource;
import io.swagger.v3.oas.models.media.Schema;


abstract class AbstractSchemaGenerator {
  protected final MetaResource metaResource;
  protected final String prefix;

  protected AbstractSchemaGenerator() {
    this.metaResource = null;
    prefix = "";
  }
  protected AbstractSchemaGenerator(MetaResource metaResource) {
    this.metaResource = metaResource;
    prefix = metaResource.getResourceType();
  }

  public String getName() {
    return  prefix + getClass().getSimpleName();
  }

  public Schema $ref() {
    return new Schema().$ref(getName());
  }

  abstract public Schema schema();
}
