package io.crnk.gen.openapi.internal.schemas;

import io.crnk.gen.openapi.internal.OASUtils;
import io.crnk.meta.model.resource.MetaResource;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.Schema;

import java.util.Arrays;
import java.util.Collections;

public class ResourceSchema {
  private final MetaResource metaResource;

  public ResourceSchema(MetaResource metaResource) {
    this.metaResource = metaResource;
  }

  public Schema schema() {
    //Defines a schema for a JSON-API resource, without the enclosing top-level document.
    return new ComposedSchema()
        .allOf(
            Arrays.asList(
                OASUtils.get$refSchema(metaResource.getResourceType() + "Reference"),
                OASUtils.get$refSchema(metaResource.getResourceType() + "Attributes"),
                new Schema()
                    .type("object")
                    .addProperties(
                        "relationships",
                        new Schema()
                            .type("object"))
                    .addProperties(
                        "links",
                        new Schema()
                            .type("object"))
                    .required(Collections.singletonList("attributes"))));
  }
}
