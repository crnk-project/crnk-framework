package io.crnk.gen.openapi.internal.schemas;

import io.crnk.gen.openapi.internal.OASUtils;
import io.crnk.meta.model.MetaElement;
import io.crnk.meta.model.MetaPrimaryKey;
import io.crnk.meta.model.resource.MetaResource;
import io.crnk.meta.model.resource.MetaResourceField;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;

import java.util.HashMap;
import java.util.Map;

public class ResourcePostAttributes {
  private final MetaResource metaResource;
  private final Map<String, Schema> attributes;


  public ResourcePostAttributes(MetaResource metaResource) {
    this.metaResource = metaResource;
    attributes = new HashMap<>();
    for (MetaElement child : metaResource.getChildren()) {
      if (child == null) {
        continue;
      } else if (child instanceof MetaPrimaryKey) {
        continue;
      } else if (((MetaResourceField) child).isPrimaryKeyAttribute()) {
        continue;
      } else if (child instanceof MetaResourceField) {
        MetaResourceField mrf = (MetaResourceField) child;
        Schema attributeSchema = OASUtils.transformMetaResourceField(mrf.getType());
        attributeSchema.nullable(mrf.isNullable());
        if (((MetaResourceField) child).isInsertable()) {
          attributes.put(mrf.getName(), attributeSchema);
        }
      }
    }
  }

  public Schema schema() {
    return new ObjectSchema()
        .addProperties(
            "attributes",
            new ObjectSchema()
                .properties(attributes));
  }
}
