package io.crnk.gen.openapi.internal.schemas;

import io.crnk.meta.model.resource.MetaResource;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;

import java.util.Arrays;

public class ResourceReference extends AbstractSchemaGenerator {

  public ResourceReference(MetaResource metaResource) {
    super(metaResource);
  }

  public Schema schema() {
    return new ObjectSchema()
        .addProperties(
            "type",
            new TypeSchema(metaResource).schema())
        .addProperties(
            "id",
            new StringSchema()
                .description("The JSON:API resource ID"))
        .required(Arrays.asList("id", "type"));
  }
}
