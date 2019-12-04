package io.crnk.gen.openapi.internal.schemas;

import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import org.junit.Assert;
import org.junit.jupiter.api.Test;


class InfoTest {

  @Test
  void schema() {
    Schema schema = new Info().schema();
    Assert.assertTrue(schema instanceof ObjectSchema);
    Assert.assertTrue(schema.getProperties().containsKey("jsonapi"));
    Assert.assertTrue(schema.getProperties().containsKey("links"));
    Assert.assertTrue(schema.getProperties().containsKey("meta"));
    Assert.assertEquals(3, schema.getProperties().size());
  }
}
