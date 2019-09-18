package io.crnk.gen.openapi.internal.parameters;

import io.swagger.v3.oas.models.parameters.Parameter;

abstract class AbstractStaticParameterGenerator {
  public String getName() {
    return getClass().getSimpleName();
  }

  public Parameter $ref() {
    return new Parameter().$ref(getName());
  }
}
