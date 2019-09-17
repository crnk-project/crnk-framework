package io.crnk.gen.openapi.internal.parameters;

import io.crnk.gen.openapi.internal.OASUtils;
import io.crnk.meta.model.MetaAttribute;
import io.crnk.meta.model.MetaElement;
import io.crnk.meta.model.resource.MetaResource;
import io.swagger.v3.oas.models.parameters.Parameter;

public class PrimaryKey {
  private final MetaResource metaResource;

  public PrimaryKey(MetaResource metaResource) {
    this.metaResource = metaResource;
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
              .schema(OASUtils.transformMetaResourceField(((MetaAttribute) metaElement).getType()));
        }
      }
    }
    return parameter;
  }
}
