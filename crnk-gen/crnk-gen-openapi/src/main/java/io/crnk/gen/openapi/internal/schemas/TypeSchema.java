package io.crnk.gen.openapi.internal.schemas;

import io.crnk.meta.model.resource.MetaResource;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;

public class TypeSchema extends AbstractSchemaGenerator {

  TypeSchema(MetaResource metaResource) {
    super(metaResource);
  }

  public Schema schema() {
    Schema typeSchema = new StringSchema()
        .description("The JSON:API resource type (" + metaResource.getResourceType() + ")");
    typeSchema.addEnumItemObject(metaResource.getResourceType());
    return typeSchema;
  }
}
