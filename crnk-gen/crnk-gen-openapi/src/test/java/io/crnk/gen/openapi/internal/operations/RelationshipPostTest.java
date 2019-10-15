package io.crnk.gen.openapi.internal.operations;

import io.crnk.gen.openapi.internal.OperationType;
import io.swagger.v3.oas.models.Operation;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

class RelationshipPostTest extends NestedOperationsBaseTest {
  @Test
  void operationType() {
    RelationshipPost RelationshipPost = new RelationshipPost(metaResource, metaResourceField, relatedMetaResource);
    Assert.assertEquals(OperationType.POST, RelationshipPost.operationType());
  }

  @Test
  void isEnabledTrueWhenReadableAndFieldInsertable() {
    RelationshipPost RelationshipPost = new RelationshipPost(metaResource, metaResourceField, relatedMetaResource);
    Assert.assertTrue(metaResource.isReadable());
    metaResourceField.setInsertable(true);
    Assert.assertTrue(RelationshipPost.isEnabled());
  }

  @Test
  void isEnabledFalseWhenReadableAndFieldNotInsertable() {
    RelationshipPost RelationshipPost = new RelationshipPost(metaResource, metaResourceField, relatedMetaResource);
    Assert.assertTrue(metaResource.isReadable());
    metaResource.setInsertable(false);
    Assert.assertFalse(RelationshipPost.isEnabled());
  }

  @Test
  void isEnabledFalseWhenNotReadableAndFieldInsertable() {
    RelationshipPost RelationshipPost = new RelationshipPost(metaResource, metaResourceField, relatedMetaResource);
    metaResource.setReadable(false);
    metaResourceField.setInsertable(true);
    Assert.assertFalse(RelationshipPost.isEnabled());
  }

  @Test
  void isEnabledFalseWhenNotReadableAndFieldNotInsertable() {
    RelationshipPost RelationshipPost = new RelationshipPost(metaResource, metaResourceField, relatedMetaResource);
    metaResource.setReadable(false);
    metaResourceField.setInsertable(false);
    Assert.assertFalse(RelationshipPost.isEnabled());
  }

  @Test
  void getDescription() {
    RelationshipPost RelationshipPost = new RelationshipPost(metaResource, metaResourceField, relatedMetaResource);
    Assert.assertEquals("Create ResourceType relationship to a RelatedResourceType resource", RelationshipPost.getDescription());
  }

  @Test
  void operation() {
    Operation operation = new RelationshipPost(metaResource, metaResourceField, relatedMetaResource).operation();
    Assert.assertTrue(operation.getResponses().containsKey("200"));
  }

  @Test
  void path() {
    RelationshipPost RelationshipPost = new RelationshipPost(metaResource, metaResourceField, relatedMetaResource);
    Assert.assertEquals("/ResourcePath/{id}/relationships/RelatedResourcePath", RelationshipPost.path());
  }
}
