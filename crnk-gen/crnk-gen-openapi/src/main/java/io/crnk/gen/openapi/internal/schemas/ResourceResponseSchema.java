package io.crnk.gen.openapi.internal.schemas;

import io.crnk.meta.model.resource.MetaResource;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.Schema;

import java.util.Arrays;
import java.util.Collections;

public class ResourceResponseSchema extends AbstractSchemaGenerator {

  public ResourceResponseSchema(MetaResource metaResource) {
    super(metaResource);
  }

  public Schema schema() {
    return new ComposedSchema()
        .allOf(
            Arrays.asList(
                new Success().$ref(),
                new Schema()
                    .addProperties(
                        "data",
                        new ResourceSchema(metaResource).$ref())
                    .required(Collections.singletonList("data"))));
  }
}
