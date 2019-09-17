package io.crnk.gen.openapi.internal.schemas;

import io.crnk.meta.model.resource.MetaResource;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;

public class TypeSchema {
  private final MetaResource metaResource;

  public TypeSchema(MetaResource metaResource) {
    this.metaResource = metaResource;
  }

  public Schema schema() {
    Schema typeSchema = new StringSchema()
        .description("The JSON:API resource type (" + metaResource.getName() + ")");
    typeSchema.addEnumItemObject(metaResource.getName());
    return typeSchema;
  }
}
