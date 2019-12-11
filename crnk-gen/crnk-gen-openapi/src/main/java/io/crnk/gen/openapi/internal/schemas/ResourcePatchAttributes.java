package io.crnk.gen.openapi.internal.schemas;

import io.crnk.gen.openapi.internal.OASUtils;
import io.crnk.meta.model.MetaElement;
import io.crnk.meta.model.resource.MetaResource;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;

import java.util.Map;
import java.util.stream.Collectors;

public class ResourcePatchAttributes extends AbstractSchemaGenerator {

  private final Map<String, Schema> attributes;

  public ResourcePatchAttributes(MetaResource metaResource) {
    super(metaResource);
    attributes = OASUtils.patchAttributes(metaResource, false)
        .collect(
            Collectors.toMap(
                MetaElement::getName,
                e -> new ResourceAttribute(metaResource, e).$ref()));
  }

  public Schema schema() {
    return new ObjectSchema()
        .addProperties(
            "attributes",
            new ObjectSchema()
                .properties(attributes));
  }
}
