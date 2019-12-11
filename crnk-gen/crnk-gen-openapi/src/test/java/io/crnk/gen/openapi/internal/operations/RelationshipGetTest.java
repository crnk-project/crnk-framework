package io.crnk.gen.openapi.internal.operations;

import io.crnk.gen.openapi.internal.OperationType;
import io.swagger.v3.oas.models.Operation;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

class RelationshipGetTest extends NestedOperationsBaseTest {
  @Test
  void operationType() {
    RelationshipGet RelationshipGet = new RelationshipGet(metaResource, metaResourceField, relatedMetaResource);
    Assert.assertEquals(OperationType.GET, RelationshipGet.operationType());
  }

  @Test
  void isEnabledTrueWhenReadableAndFieldReadable() {
    RelationshipGet RelationshipGet = new RelationshipGet(metaResource, metaResourceField, relatedMetaResource);
    Assert.assertTrue(metaResource.isReadable());
    metaResourceField.setReadable(true);
    Assert.assertTrue(RelationshipGet.isEnabled());
  }

  @Test
  void isEnabledFalseWhenReadableAndFieldNotReadable() {
    RelationshipGet RelationshipGet = new RelationshipGet(metaResource, metaResourceField, relatedMetaResource);
    Assert.assertTrue(metaResource.isReadable());
    metaResource.setReadable(false);
    Assert.assertFalse(RelationshipGet.isEnabled());
  }

  @Test
  void isEnabledFalseWhenNotReadableAndFieldReadable() {
    RelationshipGet RelationshipGet = new RelationshipGet(metaResource, metaResourceField, relatedMetaResource);
    metaResource.setReadable(false);
    metaResourceField.setReadable(true);
    Assert.assertFalse(RelationshipGet.isEnabled());
  }

  @Test
  void isEnabledFalseWhenNotReadableAndFieldNotReadable() {
    RelationshipGet RelationshipGet = new RelationshipGet(metaResource, metaResourceField, relatedMetaResource);
    metaResource.setReadable(false);
    metaResourceField.setReadable(false);
    Assert.assertFalse(RelationshipGet.isEnabled());
  }

  @Test
  void getDescription() {
    RelationshipGet RelationshipGet = new RelationshipGet(metaResource, metaResourceField, relatedMetaResource);
    Assert.assertEquals("Retrieve RelatedResourceType references related to a ResourceType resource", RelationshipGet.getDescription());
  }

  @Test
  void operation() {
    Operation operation = new RelationshipGet(metaResource, metaResourceField, relatedMetaResource).operation();
    Assert.assertTrue(operation.getResponses().containsKey("200"));
  }

  @Test
  void path() {
    RelationshipGet RelationshipGet = new RelationshipGet(metaResource, metaResourceField, relatedMetaResource);
    Assert.assertEquals("/ResourcePath/{id}/relationships/RelatedResourcePath", RelationshipGet.path());
  }
}
