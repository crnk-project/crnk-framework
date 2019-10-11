package io.crnk.gen.openapi.internal.schemas;

import io.crnk.gen.openapi.internal.OASUtils;
import io.crnk.gen.openapi.internal.annotations.OASAnnotations;
import io.crnk.meta.model.MetaAttribute;
import io.crnk.meta.model.resource.MetaResource;
import io.swagger.v3.oas.models.media.Schema;

public class ResourceAttribute extends AbstractSchemaGenerator {

  public ResourceAttribute(MetaResource metaResource, MetaAttribute metaAttribute) {
    super(metaResource, metaAttribute);
  }

  public Schema schema() {
    Schema schema = OASUtils.transformMetaResourceField(metaAttribute.getType());
    if (metaAttribute.isPrimaryKeyAttribute()) {
      schema.setDescription("The JSON:API resource ID");
    }
    schema.nullable(metaAttribute.isNullable());
    OASAnnotations.applyFromModel(schema, metaResource, metaAttribute);
    return schema;
  }
}
