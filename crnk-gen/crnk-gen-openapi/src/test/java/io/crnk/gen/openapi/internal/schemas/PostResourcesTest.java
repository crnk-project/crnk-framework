package io.crnk.gen.openapi.internal.schemas;

import io.crnk.gen.openapi.internal.MetaResourceBaseTest;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Schema;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

class PostResourcesTest extends MetaResourceBaseTest {

  @Test
  void schema() {
    Schema schema = new PostResources(metaResource).schema();
    Assert.assertTrue(schema instanceof ArraySchema);
    Schema itemSchema = ((ArraySchema) schema).getItems();
    Assert.assertEquals("#/components/schemas/ResourceTypePostResource", itemSchema.get$ref());
  }
}
