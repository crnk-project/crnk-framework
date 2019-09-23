package io.crnk.gen.openapi.internal.schemas;

import io.crnk.meta.model.resource.MetaResource;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;

public class ResourceReferenceResponseSchema extends AbstractSchemaGenerator {

  public ResourceReferenceResponseSchema(MetaResource metaResource) {
    super(metaResource);
  }

  public Schema schema() {
    return new ObjectSchema()
        .addProperties(
            "data",
            new ResourceReference(metaResource).$ref());
  }
}
