package io.crnk.gen.openapi.internal.parameters;

import io.crnk.meta.model.resource.MetaResource;
import io.crnk.meta.model.resource.MetaResourceField;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.apache.commons.lang3.StringUtils;


abstract class AbstractParameterGenerator {

  protected final MetaResource metaResource;

  final MetaResourceField metaResourceField;

  private final String prefix;

  protected AbstractParameterGenerator() {
    metaResource = null;
    metaResourceField = null;
    prefix = "";
  }

  protected AbstractParameterGenerator(MetaResource metaResource) {
    this.metaResource = metaResource;
    prefix = StringUtils.capitalize(metaResource.getResourceType());
    metaResourceField = null;
  }

  protected AbstractParameterGenerator(MetaResource metaResource, MetaResourceField metaResourceField) {
    this.metaResourceField = metaResourceField;
    this.metaResource = metaResource;
    prefix = StringUtils.capitalize(metaResource.getResourceType()) + StringUtils.capitalize(metaResourceField.getName());
  }

  public String getName() {
    return prefix + getClass().getSimpleName();
  }

  public Parameter $ref() {
    return new Parameter().$ref(getName());
  }

  abstract public Parameter parameter();
}
