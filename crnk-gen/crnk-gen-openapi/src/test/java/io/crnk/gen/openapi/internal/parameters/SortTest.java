package io.crnk.gen.openapi.internal.parameters;

import io.crnk.gen.openapi.internal.MetaResourceBaseTest;
import io.crnk.meta.model.resource.MetaResource;
import io.crnk.meta.model.resource.MetaResourceField;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

class SortTest extends MetaResourceBaseTest {

  @Test
  void parameterNoSortableAttributes() {
    MetaResource metaResource = getTestMetaResource();
    MetaResourceField metaResourceField = (MetaResourceField) metaResource.getChildren().get(0);
    MetaResourceField additionalMetaResourceField = (MetaResourceField) metaResource.getChildren().get(1);
    metaResourceField.setSortable(false);
    additionalMetaResourceField.setSortable(false);

    Parameter parameter = new Sort(metaResource).parameter();
    Assert.assertEquals("sort", parameter.getName());
    Assert.assertEquals("query", parameter.getIn());
    Assert.assertEquals("ResourceType sort order (csv)", parameter.getDescription());
    Assert.assertNull(parameter.getRequired());
    Schema schema = parameter.getSchema();
    Assert.assertTrue(schema instanceof StringSchema);
    Assert.assertEquals("", schema.getExample());
  }

  @Test
  void parameterSortableAttribute() {
    MetaResource metaResource = getTestMetaResource();
    MetaResourceField metaResourceField = (MetaResourceField) metaResource.getChildren().get(0);
    MetaResourceField additionalMetaResourceField = (MetaResourceField) metaResource.getChildren().get(1);
    metaResourceField.setSortable(false);
    additionalMetaResourceField.setSortable(true);

    Parameter parameter = new Sort(metaResource).parameter();
    Assert.assertEquals("sort", parameter.getName());
    Assert.assertEquals("query", parameter.getIn());
    Assert.assertEquals("ResourceType sort order (csv)", parameter.getDescription());
    Assert.assertNull(parameter.getRequired());
    Schema schema = parameter.getSchema();
    Assert.assertTrue(schema instanceof StringSchema);
    Assert.assertEquals("name", schema.getExample());
  }

  @Test
  void parameterSortableAttributes() {
    MetaResource metaResource = getTestMetaResource();
    MetaResourceField metaResourceField = (MetaResourceField) metaResource.getChildren().get(0);
    MetaResourceField additionalMetaResourceField = (MetaResourceField) metaResource.getChildren().get(1);
    metaResourceField.setSortable(true);
    additionalMetaResourceField.setSortable(true);

    Parameter parameter = new Sort(metaResource).parameter();
    Assert.assertEquals("sort", parameter.getName());
    Assert.assertEquals("query", parameter.getIn());
    Assert.assertEquals("ResourceType sort order (csv)", parameter.getDescription());
    Assert.assertNull(parameter.getRequired());
    Schema schema = parameter.getSchema();
    Assert.assertTrue(schema instanceof StringSchema);
    Assert.assertEquals("id,name", schema.getExample());
  }
}
