package io.crnk.gen.openapi.internal.schemas;

import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.Schema;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.util.List;

class PostResourceTest extends SchemaBaseTest {

  @Test
  void schema() {
    Schema schema = new PostResource(metaResource).schema();
    Assert.assertTrue(schema instanceof ComposedSchema);
    List<Schema> allOf = ((ComposedSchema) schema).getAllOf();
    Assert.assertEquals(2, allOf.size());
    Assert.assertEquals(
        "Post resource uses a special <Type>PostResourceReference in which the id is optional",
        "#/components/schemas/ResourceTypePostResourceReference",
        allOf.get(0).get$ref()
    );
    Assert.assertEquals("#/components/schemas/ResourceTypeResourcePostAttributes", allOf.get(1).get$ref());
  }
}
