package io.crnk.gen.openapi.internal.schemas;

import io.crnk.gen.openapi.internal.OASUtils;
import io.crnk.meta.model.resource.MetaResource;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;

public class RelationshipSingleResponseSchema {
  private final MetaResource metaResource;

  public RelationshipSingleResponseSchema(MetaResource metaResource) {
    this.metaResource = metaResource;
  }

  public Schema schema() {
    return new ObjectSchema()
        .addProperties(
            "data",
            OASUtils.get$refSchema(metaResource.getResourceType() + "Reference"));
  }
}
