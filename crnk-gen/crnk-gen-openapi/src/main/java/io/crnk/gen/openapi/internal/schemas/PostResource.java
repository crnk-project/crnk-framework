package io.crnk.gen.openapi.internal.schemas;

import io.crnk.gen.openapi.internal.OASUtils;
import io.crnk.meta.model.resource.MetaResource;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.Schema;

import java.util.Arrays;

public class PostResource {

  private final MetaResource metaResource;

  public PostResource(MetaResource metaResource) {
    this.metaResource = metaResource;
  }

  public Schema schema() {
      return new ComposedSchema()
          .allOf(
              Arrays.asList(
                  OASUtils.get$refSchema(metaResource.getResourceType() + "Reference"),
                  OASUtils.get$refSchema(metaResource.getResourceType() + "PostAttributes")));
    }
}
