package io.crnk.gen.openapi.internal.schemas;

import io.crnk.gen.openapi.internal.MetaResourceBaseTest;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import org.junit.jupiter.api.Test;

import static java.util.Collections.singleton;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ResourceAttributesTest extends MetaResourceBaseTest {

  @Test
  void schema() {
    Schema schema = new ResourceAttributes(metaResource).schema();

    assertNotNull(schema);
    assertEquals(ObjectSchema.class, schema.getClass());
    assertIterableEquals(singleton("attributes"), schema.getProperties().keySet());

    Schema attributesSchema = ((ObjectSchema) schema).getProperties().get("attributes");
    assertEquals(ObjectSchema.class, attributesSchema.getClass());
    assertIterableEquals(singleton("name"), attributesSchema.getProperties().keySet());

    Schema nameSchema = ((ObjectSchema) attributesSchema).getProperties().get("name");
    assertEquals("#/components/schemas/ResourceTypeNameResourceAttribute", nameSchema.get$ref());
  }
}
