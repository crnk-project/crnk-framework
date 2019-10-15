package io.crnk.gen.openapi.internal.schemas;

import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

class ResourceReferenceResponseSchemaTest extends SchemaBaseTest {

  @Test
  void schema() {
    Schema schema = new ResourceReferenceResponseSchema(metaResource).schema();
    Assert.assertTrue(schema instanceof ObjectSchema);
    Assert.assertTrue(schema.getProperties().containsKey("data"));
    Assert.assertEquals(1, schema.getProperties().size());

    Schema data = (Schema) schema.getProperties().get("data");
    Assert.assertEquals(
        "#/components/schemas/ResourceTypeResourceReference",
        data.get$ref()
    );
  }
}
