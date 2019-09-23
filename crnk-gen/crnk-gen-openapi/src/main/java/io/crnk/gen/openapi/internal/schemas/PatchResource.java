package io.crnk.gen.openapi.internal.schemas;

import io.crnk.meta.model.resource.MetaResource;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.Schema;

import java.util.Arrays;

public class PatchResource extends AbstractSchemaGenerator {

  public PatchResource(MetaResource metaResource) {
    super(metaResource);
  }

  public Schema schema() {
    return new ComposedSchema()
        .allOf(
            Arrays.asList(
                new ResourceReference(metaResource).$ref(),
                new ResourcePatchAttributes(metaResource).$ref()));
  }
}
