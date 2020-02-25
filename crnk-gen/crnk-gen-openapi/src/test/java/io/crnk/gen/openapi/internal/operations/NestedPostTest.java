package io.crnk.gen.openapi.internal.operations;

import io.crnk.gen.openapi.internal.OperationType;
import io.swagger.v3.oas.models.Operation;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

class NestedPostTest extends NestedOperationsBaseTest {
  @Test
  void operationType() {
    NestedPost NestedPost = new NestedPost(metaResource, metaResourceField, relatedMetaResource);
    Assert.assertEquals(OperationType.POST, NestedPost.operationType());
  }

  @Test
  void isEnabledTrueWhenReadableAndFieldInsertable() {
    NestedPost NestedPost = new NestedPost(metaResource, metaResourceField, relatedMetaResource);
    Assert.assertTrue(metaResource.isReadable());
    metaResourceField.setInsertable(true);
    Assert.assertTrue(NestedPost.isEnabled());
  }

  @Test
  void isEnabledFalseWhenReadableAndFieldNotInsertable() {
    NestedPost NestedPost = new NestedPost(metaResource, metaResourceField, relatedMetaResource);
    Assert.assertTrue(metaResource.isReadable());
    metaResource.setInsertable(false);
    Assert.assertFalse(NestedPost.isEnabled());
  }

  @Test
  void isEnabledFalseWhenNotReadableAndFieldInsertable() {
    NestedPost NestedPost = new NestedPost(metaResource, metaResourceField, relatedMetaResource);
    metaResource.setReadable(false);
    metaResourceField.setInsertable(true);
    Assert.assertFalse(NestedPost.isEnabled());
  }

  @Test
  void isEnabledFalseWhenNotReadableAndFieldNotInsertable() {
    NestedPost NestedPost = new NestedPost(metaResource, metaResourceField, relatedMetaResource);
    metaResource.setReadable(false);
    metaResourceField.setInsertable(false);
    Assert.assertFalse(NestedPost.isEnabled());
  }

  @Test
  void getDescription() {
    NestedPost NestedPost = new NestedPost(metaResource, metaResourceField, relatedMetaResource);
    Assert.assertEquals("Create ResourceType relationship to a RelatedResourceType resource", NestedPost.getDescription());
  }

  @Test
  void operation() {
    Operation operation = new NestedPost(metaResource, metaResourceField, relatedMetaResource).operation();
    Assert.assertTrue(operation.getResponses().containsKey("200"));
  }

  @Test
  void path() {
    NestedPost NestedPost = new NestedPost(metaResource, metaResourceField, relatedMetaResource);
    Assert.assertEquals("/ResourcePath/{id}/someRelatedResource", NestedPost.path());
  }
}
