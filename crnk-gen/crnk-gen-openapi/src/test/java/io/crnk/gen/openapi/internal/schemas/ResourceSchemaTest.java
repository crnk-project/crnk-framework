package io.crnk.gen.openapi.internal.schemas;

import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.util.List;

class ResourceSchemaTest extends SchemaBaseTest {

  @Test
  void schema() {
    Schema schema = new ResourceSchema(metaResource).schema();
    Assert.assertTrue(schema instanceof ComposedSchema);
    Assert.assertEquals(1, schema.getRequired().size());
    Assert.assertTrue(schema.getRequired().contains("attributes"));
    
    List<Schema> allOf = ((ComposedSchema) schema).getAllOf();
    Assert.assertEquals(3, allOf.size());
    Assert.assertEquals("#/components/schemas/ResourceTypeResourceReference", allOf.get(0).get$ref());
    Assert.assertEquals("#/components/schemas/ResourceTypeResourceAttributes", allOf.get(1).get$ref());

    Schema linksAndRelationships = allOf.get(2);
    Assert.assertTrue(linksAndRelationships instanceof ObjectSchema);
    Assert.assertEquals(2, linksAndRelationships.getProperties().size());
    Assert.assertTrue(linksAndRelationships.getProperties().containsKey("links"));
    Assert.assertTrue(linksAndRelationships.getProperties().containsKey("relationships"));
  }
}
