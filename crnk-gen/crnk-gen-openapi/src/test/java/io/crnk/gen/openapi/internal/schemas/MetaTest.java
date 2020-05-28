package io.crnk.gen.openapi.internal.schemas;

import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import org.junit.Assert;
import org.junit.jupiter.api.Test;


class MetaTest {

  @Test
  void schema() {
    Schema schema = new Meta().schema();
    Assert.assertTrue(schema instanceof ObjectSchema);
    Assert.assertTrue((boolean) schema.getAdditionalProperties());
    Assert.assertNull(schema.getProperties());
  }
}
