package io.crnk.gen.openapi.internal.schemas;

import io.crnk.meta.model.MetaAttribute;
import io.crnk.meta.model.resource.MetaResource;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;

import java.util.Collections;

import static io.crnk.gen.openapi.internal.OASUtils.getPrimaryKeyMetaResourceField;

public class PostResourceReference extends AbstractSchemaGenerator {

  public PostResourceReference(MetaResource metaResource) {
    super(metaResource);
  }

  public Schema schema() {
    MetaAttribute metaAttribute = getPrimaryKeyMetaResourceField(metaResource);
    return new ObjectSchema()
        .addProperties(
            "type",
            new TypeSchema(metaResource).schema())
        .addProperties(
            "id",
            new ResourceAttribute(metaResource, metaAttribute).$ref())
        .required(Collections.singletonList("type"));
  }
}
