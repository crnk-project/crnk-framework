package io.crnk.gen.openapi.internal.schemas;

import io.crnk.gen.openapi.internal.OASUtils;
import io.crnk.meta.model.resource.MetaResource;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.Schema;

import java.util.Arrays;
import java.util.Collections;

public class ResourcesResponseSchema {
  private final MetaResource metaResource;

  public ResourcesResponseSchema(MetaResource metaResource) {
    this.metaResource = metaResource;
  }

  public Schema schema() {
    return new ComposedSchema()
        .allOf(
            Arrays.asList(
                OASUtils.get$refSchema("ListResponseMixin"),
                new Schema()
                    .addProperties(
                        "data",
                        new ArraySchema()
                            .items(
                                OASUtils.get$refSchema(metaResource.getName())))
                    .required(Collections.singletonList("data"))));
  }
}
