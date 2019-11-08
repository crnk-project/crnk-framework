package io.crnk.gen.openapi.internal.parameters;

import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

class NestedFilterTest {

  @Test
  void parameter() {
    Parameter parameter = new NestedFilter().parameter();
    Assert.assertEquals("filter", parameter.getName());
    Assert.assertEquals("query", parameter.getIn());
    Assert.assertEquals("Customizable query (experimental)", parameter.getDescription());
    Assert.assertNull(parameter.getRequired());
    Schema schema = parameter.getSchema();
    Assert.assertTrue(schema instanceof ObjectSchema);
    Assert.assertEquals(true, schema.getAdditionalProperties());
    Assert.assertEquals(3, schema.getProperties().size());
    ObjectSchema andSchema = (ObjectSchema) schema.getProperties().get("AND");
    Assert.assertEquals(true, andSchema.getAdditionalProperties());
    Assert.assertTrue(andSchema.getNullable());
    ObjectSchema orSchema = (ObjectSchema) schema.getProperties().get("OR");
    Assert.assertEquals(true, orSchema.getAdditionalProperties());
    Assert.assertTrue(orSchema.getNullable());
    ObjectSchema notSchema = (ObjectSchema) schema.getProperties().get("NOT");
    Assert.assertEquals(true, notSchema.getAdditionalProperties());
    Assert.assertTrue(notSchema.getNullable());
  }
}
