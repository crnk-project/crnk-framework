package io.crnk.gen.openapi.internal.parameters;

import io.crnk.gen.openapi.internal.OASUtils;
import io.crnk.meta.model.MetaElement;
import io.crnk.meta.model.resource.MetaResource;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;

import static java.util.stream.Collectors.joining;

public class Sort extends AbstractParameterGenerator {

  public Sort(MetaResource metaResource) {
    super(metaResource);
  }

  public Parameter parameter() {
    return new Parameter()
        .name("sort")
        .description(metaResource.getResourceType() + " sort order (csv)")
        .in("query")
        .schema(new StringSchema()
            .example(
                OASUtils.sortAttributes(metaResource, true)
                    .map(MetaElement::getName)
                    .collect(joining(","))));
  }
}
