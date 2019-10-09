package io.crnk.gen.openapi.internal.parameters;

import io.crnk.gen.openapi.internal.schemas.ResourceAttribute;
import io.crnk.meta.model.MetaAttribute;
import io.crnk.meta.model.MetaElement;
import io.crnk.meta.model.resource.MetaResource;
import io.swagger.v3.oas.models.parameters.Parameter;

public class PrimaryKey extends AbstractParameterGenerator {

  public PrimaryKey(MetaResource metaResource) {
    super(metaResource);
  }

  public Parameter parameter() {
    Parameter parameter = new Parameter();
    for (MetaElement metaElement : metaResource.getChildren()) {
      if (metaElement instanceof MetaAttribute) {
        MetaAttribute metaAttribute = (MetaAttribute) metaElement;
        if (metaAttribute.isPrimaryKeyAttribute()) {
          parameter = parameter
              .name(metaElement.getName())
              .in("path")
              .schema(new ResourceAttribute(metaResource, metaAttribute).$ref());
        }
      }
    }
    return parameter;
  }
}
