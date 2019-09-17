package io.crnk.gen.openapi.internal.parameters;

import io.crnk.meta.model.MetaAttribute;
import io.crnk.meta.model.MetaElement;
import io.crnk.meta.model.resource.MetaResource;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;

import static java.util.stream.Collectors.joining;

public class Sort {
  private final MetaResource metaResource;

  public Sort(MetaResource metaResource) {
    this.metaResource = metaResource;
  }

  public Parameter parameter() {
    return new Parameter()
        .name("sort")
        .description(metaResource.getResourceType() + " sort order (csv)")
        .in("query")
        .schema(new StringSchema()
            .example(
                metaResource
                    .getAttributes()
                    .stream()
                    .filter(MetaAttribute::isSortable)
                    .map(MetaElement::getName)
                    .collect(joining(","))));
  }
}
