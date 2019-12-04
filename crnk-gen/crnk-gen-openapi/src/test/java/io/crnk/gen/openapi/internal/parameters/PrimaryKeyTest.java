package io.crnk.gen.openapi.internal.parameters;

import io.crnk.gen.openapi.internal.MetaResourceBaseTest;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

class PrimaryKeyTest extends MetaResourceBaseTest {

  @Test
  void parameter() {
    Parameter parameter = new PrimaryKey(metaResource).parameter();
    Assert.assertEquals("id", parameter.getName());
    Assert.assertEquals("path", parameter.getIn());
    Assert.assertTrue(parameter.getRequired());
    Schema schema = parameter.getSchema();
    Assert.assertEquals(
        "#/components/schemas/ResourceTypeIdResourceAttribute",
        schema.get$ref()
    );
  }
}
