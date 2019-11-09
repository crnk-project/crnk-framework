package io.crnk.gen.openapi.internal.schemas;

import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;


class LinkTest {

  @Test
  void schema() {
    Schema schema = new Link().schema();
    Assert.assertTrue(schema instanceof ComposedSchema);

    List<Schema> oneOf = ((ComposedSchema) schema).getOneOf();
    Assert.assertEquals(2, oneOf.size());

    StringSchema stringSchema = (StringSchema) oneOf.get(0);
    Assert.assertEquals("uri", stringSchema.getFormat());

    ObjectSchema objectSchema = (ObjectSchema) oneOf.get(1);
    Assert.assertEquals(Collections.singletonList("href"), objectSchema.getRequired());
    Assert.assertTrue(objectSchema.getProperties().containsKey("href"));
    Assert.assertTrue(objectSchema.getProperties().containsKey("meta"));
    Assert.assertEquals(2, objectSchema.getProperties().size());
  }
}
