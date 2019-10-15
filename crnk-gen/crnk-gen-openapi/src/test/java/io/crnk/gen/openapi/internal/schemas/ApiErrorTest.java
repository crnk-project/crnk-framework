package io.crnk.gen.openapi.internal.schemas;

import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import org.junit.Assert;
import org.junit.jupiter.api.Test;


class ApiErrorTest extends SchemaBaseTest {

  @Test
  void schema() {
    Schema schema = new ApiError().schema();
    Assert.assertTrue(schema instanceof ObjectSchema);
    Assert.assertTrue(schema.getProperties().containsKey("id"));
    Assert.assertTrue(schema.getProperties().containsKey("links"));
    Assert.assertTrue(schema.getProperties().containsKey("status"));
    Assert.assertTrue(schema.getProperties().containsKey("code"));
    Assert.assertTrue(schema.getProperties().containsKey("title"));
    Assert.assertTrue(schema.getProperties().containsKey("title"));
    Assert.assertTrue(schema.getProperties().containsKey("detail"));
    Assert.assertTrue(schema.getProperties().containsKey("source"));
    Assert.assertTrue(schema.getProperties().containsKey("meta"));
    Assert.assertEquals(8, schema.getProperties().size());
  }
}
