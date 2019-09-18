package io.crnk.gen.openapi.internal.parameters;

import io.crnk.meta.model.resource.MetaResourceField;
import io.swagger.v3.oas.models.parameters.Parameter;


abstract class AbstractFieldParameterGenerator {
  protected final MetaResourceField metaResourceField;

  protected AbstractFieldParameterGenerator(MetaResourceField metaResourceField) {
    this.metaResourceField = metaResourceField;
  }

  String getName() {
    return metaResourceField.getName() + getClass().getSimpleName();
  }

  public Parameter $ref() {
    return new Parameter().$ref(getName());
  }
}
