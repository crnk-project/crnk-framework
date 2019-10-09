package io.crnk.gen.openapi.internal.schemas;

import io.crnk.meta.model.resource.MetaResource;
import io.swagger.v3.oas.models.media.Schema;

import java.util.Arrays;

public class ResourceReference extends AbstractSchemaGenerator {

  public ResourceReference(MetaResource metaResource) {
    super(metaResource);
  }

  public Schema schema() {
    return new PostResourceReference(metaResource)
        .schema()
        .required(Arrays.asList("id", "type"));
  }
}
