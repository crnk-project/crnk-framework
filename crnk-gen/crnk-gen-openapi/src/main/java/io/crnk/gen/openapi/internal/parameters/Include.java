package io.crnk.gen.openapi.internal.parameters;

import io.crnk.meta.model.MetaAttribute;
import io.crnk.meta.model.resource.MetaResource;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;

import static java.util.stream.Collectors.joining;

public class Include extends AbstractResourceParameterGenerator{

  public Include(MetaResource metaResource) {
    super(metaResource);
  }

  public Parameter parameter() {
    return new Parameter()
        .name("include")
        .description(metaResource.getResourceType() + " relationships to include (csv)")
        .in("query")
        .schema(new StringSchema()
            ._default(
                metaResource
                    .getAttributes()
                    .stream()
                    .filter(MetaAttribute::isAssociation)
                    .map(e -> e.getType().getElementType().getName())
                    .collect(joining(","))));
  }
}
