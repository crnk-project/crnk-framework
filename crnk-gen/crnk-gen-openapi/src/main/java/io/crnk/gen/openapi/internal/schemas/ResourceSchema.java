package io.crnk.gen.openapi.internal.schemas;

import io.crnk.meta.model.resource.MetaResource;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;

import java.util.Arrays;
import java.util.Collections;

public class ResourceSchema extends AbstractSchemaGenerator {

  public ResourceSchema(MetaResource metaResource) {
    super(metaResource);
  }

  public Schema schema() {
    //Defines a schema for a JSON-API resource, without the enclosing top-level document.
    return new ComposedSchema()
        .allOf(
            Arrays.asList(
                new ResourceReference(metaResource).$ref(),
                new ResourceAttributes(metaResource).$ref(),
                new ObjectSchema()
                    .addProperties(
                        "relationships",
                        new ObjectSchema())
                    .addProperties(
                        "links",
                        new ObjectSchema())
                    .required(Collections.singletonList("attributes"))));
  }
}
