package io.crnk.gen.openapi.internal.schemas;

import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

class PostResourceReferenceTest extends SchemaBaseTest {

  @Test
  void schema() {
    Schema schema = new PostResourceReference(metaResource).schema();
    Assert.assertTrue(schema instanceof ObjectSchema);
    Assert.assertTrue(schema.getProperties().containsKey("type"));
    Assert.assertTrue(schema.getProperties().containsKey("id"));
    Assert.assertEquals(2, schema.getProperties().size());
    Assert.assertEquals(schema.getRequired().get(0), "type");
    Assert.assertEquals(1, schema.getRequired().size());
  }
}
