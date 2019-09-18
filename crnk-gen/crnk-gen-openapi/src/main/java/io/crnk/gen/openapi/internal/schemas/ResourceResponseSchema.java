package io.crnk.gen.openapi.internal.schemas;

import io.crnk.meta.model.resource.MetaResource;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.Schema;

import java.util.Arrays;
import java.util.Collections;

public class ResourceResponseSchema extends AbstractResourceSchemaGenerator {

  public ResourceResponseSchema(MetaResource metaResource) {
    super(metaResource);
  }

  public Schema schema() {
    return new ComposedSchema()
        .allOf(
            Arrays.asList(
                ResponseMixin.$ref(),
                new Schema()
                    .addProperties(
                        "data",
                        new ArraySchema()
                            .items(new ResourceSchema(metaResource).$ref()))
                    .required(Collections.singletonList("data"))));
  }
}
