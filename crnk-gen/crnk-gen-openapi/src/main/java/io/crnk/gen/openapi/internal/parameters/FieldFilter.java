package io.crnk.gen.openapi.internal.parameters;

import io.crnk.meta.model.resource.MetaResource;
import io.crnk.meta.model.resource.MetaResourceField;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;

public class FieldFilter extends AbstractParameterGenerator {

  public FieldFilter(MetaResource metaResource, MetaResourceField metaResourceField) {
    super(metaResource, metaResourceField);
  }

  public Parameter parameter() {
    return new Parameter()
        .name("filter[" + metaResourceField.getName() + "]")
        .description("Filter by " + metaResourceField.getName() + " (csv)")
        .in("query")
        .schema(new StringSchema());
  }
}
