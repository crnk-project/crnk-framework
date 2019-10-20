package io.crnk.gen.openapi.internal.schemas;

import io.crnk.gen.openapi.internal.MetaResourceBaseTest;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.Schema;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.util.List;

class ResourceResponseSchemaTest extends MetaResourceBaseTest {

  @Test
  void schema() {
    Schema schema = new ResourceResponseSchema(metaResource).schema();
    Assert.assertTrue(schema instanceof ComposedSchema);
    List<Schema> allOf = ((ComposedSchema) schema).getAllOf();
    Assert.assertEquals(2, allOf.size());
    Assert.assertEquals("#/components/schemas/ResponseMixin", allOf.get(0).get$ref());

    Schema dataSchema = allOf.get(1);
    Schema data = (Schema) dataSchema.getProperties().get("data");
    Assert.assertEquals(
        "#/components/schemas/ResourceTypeResourceSchema",
        data.get$ref()
    );
  }
}
