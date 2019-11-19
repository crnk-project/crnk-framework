package io.crnk.gen.openapi.internal.schemas;

import io.crnk.meta.model.resource.MetaResource;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Schema;

public class PostResources extends AbstractSchemaGenerator {

  public PostResources(MetaResource metaResource) {
    super(metaResource);
  }

  public Schema schema() {
    return new ArraySchema().items(new PostResource(metaResource).$ref());
  }
}
