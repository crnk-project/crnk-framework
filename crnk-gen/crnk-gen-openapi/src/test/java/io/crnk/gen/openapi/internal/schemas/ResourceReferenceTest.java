package io.crnk.gen.openapi.internal.schemas;

import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

class ResourceReferenceTest extends SchemaBaseTest {

  @Test
  void schema() {
    Schema schema = new ResourceReference(metaResource).schema();
    Assert.assertTrue(schema instanceof ObjectSchema);
    Assert.assertTrue(schema.getProperties().containsKey("id"));
    Assert.assertTrue(schema.getProperties().containsKey("type"));
    Assert.assertEquals(2, schema.getProperties().size());
    Assert.assertTrue(schema.getRequired().contains("id"));
    Assert.assertTrue(schema.getRequired().contains("type"));
    Assert.assertEquals(2, schema.getRequired().size());
  }
}
