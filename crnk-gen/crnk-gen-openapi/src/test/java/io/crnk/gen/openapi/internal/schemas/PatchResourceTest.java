package io.crnk.gen.openapi.internal.schemas;

import io.crnk.gen.openapi.internal.MetaResourceBaseTest;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.Schema;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.util.List;

class PatchResourceTest extends MetaResourceBaseTest {

  @Test
  void schema() {
    Schema schema = new PatchResource(metaResource).schema();
    Assert.assertTrue(schema instanceof ComposedSchema);
    List<Schema> allOf = ((ComposedSchema) schema).getAllOf();
    Assert.assertEquals(2, allOf.size());
    Assert.assertEquals("#/components/schemas/ResourceTypeResourceReference", allOf.get(0).get$ref());
    Assert.assertEquals("#/components/schemas/ResourceTypeResourcePatchAttributes", allOf.get(1).get$ref());
  }
}
