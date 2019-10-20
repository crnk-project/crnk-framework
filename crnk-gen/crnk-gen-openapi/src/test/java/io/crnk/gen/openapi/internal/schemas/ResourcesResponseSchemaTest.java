package io.crnk.gen.openapi.internal.schemas;

import io.crnk.gen.openapi.internal.MetaResourceBaseTest;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.Schema;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.util.List;

class ResourcesResponseSchemaTest extends MetaResourceBaseTest {

  @Test
  void schema() {
    Schema schema = new ResourcesResponseSchema(metaResource).schema();
    Assert.assertTrue(schema instanceof ComposedSchema);
    List<Schema> allOf = ((ComposedSchema) schema).getAllOf();
    Assert.assertEquals(2, allOf.size());
    Assert.assertEquals("#/components/schemas/ListResponseMixin", allOf.get(0).get$ref());

    Schema dataSchema = allOf.get(1);
    Assert.assertEquals(1, dataSchema.getRequired().size());
    Assert.assertTrue(dataSchema.getRequired().contains("data"));
    Schema data = (Schema) dataSchema.getProperties().get("data");
    Assert.assertTrue(data instanceof ArraySchema);
    Assert.assertEquals(
        "#/components/schemas/ResourceTypeResourceSchema",
        ((ArraySchema) data).getItems().get$ref()
    );
  }
}
