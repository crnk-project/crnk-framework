package io.crnk.gen.openapi.internal.operations;

import io.crnk.gen.openapi.internal.OperationType;
import io.swagger.v3.oas.models.Operation;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

class NestedGetTest extends NestedOperationsBaseTest {
  @Test
  void operationType() {
    NestedGet NestedGet = new NestedGet(metaResource, metaResourceField, relatedMetaResource);
    Assert.assertEquals(OperationType.GET, NestedGet.operationType());
  }

  @Test
  void isEnabledTrueWhenReadableAndFieldReadable() {
    NestedGet NestedGet = new NestedGet(metaResource, metaResourceField, relatedMetaResource);
    Assert.assertTrue(metaResource.isReadable());
    metaResourceField.setReadable(true);
    Assert.assertTrue(NestedGet.isEnabled());
  }

  @Test
  void isEnabledFalseWhenReadableAndFieldNotReadable() {
    NestedGet NestedGet = new NestedGet(metaResource, metaResourceField, relatedMetaResource);
    Assert.assertTrue(metaResource.isReadable());
    metaResource.setReadable(false);
    Assert.assertFalse(NestedGet.isEnabled());
  }

  @Test
  void isEnabledFalseWhenNotReadableAndFieldReadable() {
    NestedGet NestedGet = new NestedGet(metaResource, metaResourceField, relatedMetaResource);
    metaResource.setReadable(false);
    metaResourceField.setReadable(true);
    Assert.assertFalse(NestedGet.isEnabled());
  }

  @Test
  void isEnabledFalseWhenNotReadableAndFieldNotReadable() {
    NestedGet NestedGet = new NestedGet(metaResource, metaResourceField, relatedMetaResource);
    metaResource.setReadable(false);
    metaResourceField.setReadable(false);
    Assert.assertFalse(NestedGet.isEnabled());
  }

  @Test
  void getDescription() {
    NestedGet NestedGet = new NestedGet(metaResource, metaResourceField, relatedMetaResource);
    Assert.assertEquals("Retrieve RelatedResourceType related to a ResourceType resource", NestedGet.getDescription());
  }

  @Test
  void operation() {
    Operation operation = new NestedGet(metaResource, metaResourceField, relatedMetaResource).operation();
    Assert.assertTrue(operation.getResponses().containsKey("200"));
  }

  @Test
  void path() {
    NestedGet NestedGet = new NestedGet(metaResource, metaResourceField, relatedMetaResource);
    Assert.assertEquals("/ResourcePath/{id}/someRelatedResource", NestedGet.path());
  }
}
