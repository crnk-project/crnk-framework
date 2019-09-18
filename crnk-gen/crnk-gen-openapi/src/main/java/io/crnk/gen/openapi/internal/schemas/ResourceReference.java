package io.crnk.gen.openapi.internal.schemas;

import io.crnk.meta.model.resource.MetaResource;
import io.swagger.v3.oas.models.media.Schema;

import java.util.Arrays;

public class ResourceReference extends AbstractResourceSchemaGenerator {

  public ResourceReference(MetaResource metaResource) {
    super(metaResource);
  }

  public Schema schema() {
    return new Schema()
        .type("object")
        .addProperties(
            "type",
            new TypeSchema(metaResource).schema())
        .addProperties(
            "id",
            new Schema()
                .type("string")
                .description("The JSON:API resource ID"))
        .required(Arrays.asList("id", "type"));
  }
}
