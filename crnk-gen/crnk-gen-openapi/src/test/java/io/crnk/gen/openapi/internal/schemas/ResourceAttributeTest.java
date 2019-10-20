package io.crnk.gen.openapi.internal.schemas;

import io.crnk.gen.openapi.internal.MetaResourceBaseTest;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

class ResourceAttributeTest extends MetaResourceBaseTest {

  @Test
  void schemaPrimaryKey() {
    Schema schema = new ResourceAttribute(metaResource, metaResourceField).schema();
    Assert.assertTrue(schema instanceof StringSchema);
    Assert.assertEquals("The JSON:API resource ID", schema.getDescription());
  }

  @Test
  void schema() {
    Schema schema = new ResourceAttribute(metaResource, additionalMetaResourceField).schema();
    Assert.assertTrue(schema instanceof StringSchema);
    Assert.assertNull(schema.getDescription());
  }

  @Test
  void schemaNullable() {
    additionalMetaResourceField.setNullable(true);
    Schema schema = new ResourceAttribute(metaResource, additionalMetaResourceField).schema();
    Assert.assertTrue(schema instanceof StringSchema);
    Assert.assertTrue(schema.getNullable());
  }

  @Test
  void notNullable() {
    additionalMetaResourceField.setNullable(false);
    Schema schema = new ResourceAttribute(metaResource, additionalMetaResourceField).schema();
    Assert.assertTrue(schema instanceof StringSchema);
    Assert.assertFalse(schema.getNullable());
  }
}
