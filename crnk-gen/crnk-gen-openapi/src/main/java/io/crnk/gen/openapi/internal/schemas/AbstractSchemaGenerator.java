package io.crnk.gen.openapi.internal.schemas;

import io.crnk.meta.model.MetaAttribute;
import io.crnk.meta.model.resource.MetaResource;
import io.swagger.v3.oas.models.media.Schema;


abstract class AbstractSchemaGenerator {

  protected final MetaResource metaResource;

  protected final MetaAttribute metaAttribute;

  private final String prefix;

  protected AbstractSchemaGenerator() {
    this.metaResource = null;
    this.metaAttribute = null;
    prefix = "";
  }

  protected AbstractSchemaGenerator(MetaResource metaResource) {
    this.metaResource = metaResource;
    this.metaAttribute = null;
    prefix = metaResource.getResourceType();
  }

  protected AbstractSchemaGenerator(MetaResource metaResource, MetaAttribute metaAttribute) {
    this.metaResource = metaResource;
    this.metaAttribute = metaAttribute;
    prefix = metaResource.getResourceType() + metaAttribute.getName();
  }

  public String getName() {
    return prefix + getClass().getSimpleName();
  }

  public Schema $ref() {
    return new Schema().$ref(getName());
  }

  abstract public Schema schema();
}
