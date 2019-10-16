package io.crnk.gen.openapi.internal.schemas;

import io.crnk.gen.openapi.internal.MetaResourceBaseTest;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

class ResourcePostAttributesTest extends MetaResourceBaseTest {

  @Test
  void isInsertable() {
    additionalMetaResourceField.setInsertable(true);
    Schema schema = new ResourcePostAttributes(metaResource).schema();
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
  void notInsertable() {
    additionalMetaResourceField.setInsertable(false);
    Schema schema = new ResourcePostAttributes(metaResource).schema();
    Assert.assertTrue(schema instanceof ObjectSchema);

    Assert.assertTrue(schema.getProperties().containsKey("attributes"));
    Assert.assertEquals(1, schema.getProperties().size());

    Schema attributes = (Schema) schema.getProperties().get("attributes");
    Assert.assertTrue(attributes instanceof ObjectSchema);
    Assert.assertEquals(0, attributes.getProperties().size());
  }
}
