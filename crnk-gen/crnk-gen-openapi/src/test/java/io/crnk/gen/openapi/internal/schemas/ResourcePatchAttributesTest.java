package io.crnk.gen.openapi.internal.schemas;

import io.crnk.gen.openapi.internal.MetaResourceBaseTest;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

class ResourcePatchAttributesTest extends MetaResourceBaseTest {

  @Test
  void isUpdatable() {
    additionalMetaResourceField.setUpdatable(true);
    Schema schema = new ResourcePatchAttributes(metaResource).schema();
    Assert.assertTrue(schema instanceof ObjectSchema);

    Assert.assertTrue(schema.getProperties().containsKey("attributes"));
    Assert.assertEquals(1, schema.getProperties().size());

    Schema attributes = (Schema) schema.getProperties().get("attributes");
    Assert.assertTrue(attributes instanceof ObjectSchema);
    Assert.assertTrue(attributes.getProperties().containsKey("name"));
    Assert.assertEquals(1, attributes.getProperties().size());

    Schema name = (Schema) attributes.getProperties().get("name");
    Assert.assertEquals(
        "#/components/schemas/ResourceTypeNameResourceAttribute",
        name.get$ref());
  }

  @Test
  void notUpdatable() {
    additionalMetaResourceField.setUpdatable(false);
    Schema schema = new ResourcePatchAttributes(metaResource).schema();
    Assert.assertTrue(schema instanceof ObjectSchema);

    Assert.assertTrue(schema.getProperties().containsKey("attributes"));
    Assert.assertEquals(1, schema.getProperties().size());

    Schema attributes = (Schema) schema.getProperties().get("attributes");
    Assert.assertTrue(attributes instanceof ObjectSchema);
    Assert.assertEquals(0, attributes.getProperties().size());
  }
}
