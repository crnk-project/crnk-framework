package io.crnk.gen.openapi.internal.operations;

import io.crnk.gen.openapi.internal.OperationType;
import io.swagger.v3.oas.models.Operation;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

class NestedPatchTest extends NestedOperationsBaseTest {
  @Test
  void operationType() {
    NestedPatch NestedPatch = new NestedPatch(metaResource, metaResourceField, relatedMetaResource);
    Assert.assertEquals(OperationType.PATCH, NestedPatch.operationType());
  }

  @Test
  void isEnabledTrueWhenReadableAndFieldUpdatable() {
    NestedPatch NestedPatch = new NestedPatch(metaResource, metaResourceField, relatedMetaResource);
    Assert.assertTrue(metaResource.isReadable());
    metaResourceField.setUpdatable(true);
    Assert.assertTrue(NestedPatch.isEnabled());
  }

  @Test
  void isEnabledFalseWhenReadableAndFieldNotUpdatable() {
    NestedPatch NestedPatch = new NestedPatch(metaResource, metaResourceField, relatedMetaResource);
    Assert.assertTrue(metaResource.isReadable());
    metaResource.setUpdatable(false);
    Assert.assertFalse(NestedPatch.isEnabled());
  }

  @Test
  void isEnabledFalseWhenNotReadableAndFieldUpdatable() {
    NestedPatch NestedPatch = new NestedPatch(metaResource, metaResourceField, relatedMetaResource);
    metaResource.setReadable(false);
    metaResourceField.setUpdatable(true);
    Assert.assertFalse(NestedPatch.isEnabled());
  }

  @Test
  void isEnabledFalseWhenNotReadableAndFieldNotUpdatable() {
    NestedPatch NestedPatch = new NestedPatch(metaResource, metaResourceField, relatedMetaResource);
    metaResource.setReadable(false);
    metaResourceField.setUpdatable(false);
    Assert.assertFalse(NestedPatch.isEnabled());
  }

  @Test
  void getDescription() {
    NestedPatch NestedPatch = new NestedPatch(metaResource, metaResourceField, relatedMetaResource);
    Assert.assertEquals("Update ResourceType relationship to a RelatedResourceType resource", NestedPatch.getDescription());
  }

  @Test
  void operation() {
    Operation operation = new NestedPatch(metaResource, metaResourceField, relatedMetaResource).operation();
    Assert.assertTrue(operation.getResponses().containsKey("200"));
  }

  @Test
  void path() {
    NestedPatch NestedPatch = new NestedPatch(metaResource, metaResourceField, relatedMetaResource);
    Assert.assertEquals("/ResourcePath/{id}/someRelatedResource", NestedPatch.path());
  }
}
