package io.crnk.gen.openapi.internal.parameters;

import io.crnk.gen.openapi.internal.OASUtils;
import io.crnk.gen.openapi.internal.schemas.ResourceAttribute;
import io.crnk.meta.model.resource.MetaResource;
import io.crnk.meta.model.resource.MetaResourceField;
import io.swagger.v3.oas.models.parameters.Parameter;

public class PrimaryKey extends AbstractParameterGenerator {

  public PrimaryKey(MetaResource metaResource) {
    super(metaResource);
  }

  public Parameter parameter() {
    MetaResourceField metaResourceField = OASUtils.getPrimaryKeyMetaResourceField(metaResource);
    return new Parameter()
        .name(metaResourceField.getName())
        .in("path")
        .schema(new ResourceAttribute(metaResource, metaResourceField).$ref());
  }
}
