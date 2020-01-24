package io.crnk.gen.openapi.internal.schemas;

import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import org.junit.Assert;
import org.junit.jupiter.api.Test;


class JsonApiTest {

  @Test
  void schema() {
    Schema schema = new JsonApi().schema();
    Assert.assertTrue(schema instanceof ObjectSchema);
    Assert.assertTrue(schema.getProperties().containsKey("version"));
    Assert.assertEquals(1, schema.getProperties().size());
  }
}
