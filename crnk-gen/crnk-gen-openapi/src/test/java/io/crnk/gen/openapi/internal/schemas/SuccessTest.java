package io.crnk.gen.openapi.internal.schemas;

import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import org.junit.Assert;
import org.junit.jupiter.api.Test;


class SuccessTest {

  @Test
  void schema() {
    Schema schema = new Success().schema();
    Assert.assertTrue(schema instanceof ObjectSchema);
    Assert.assertTrue(schema.getProperties().containsKey("jsonapi"));
    Assert.assertTrue(schema.getProperties().containsKey("links"));
    Assert.assertTrue(schema.getProperties().containsKey("meta"));
    Assert.assertTrue(schema.getProperties().containsKey("included"));
    Assert.assertEquals(4, schema.getProperties().size());
  }
}
