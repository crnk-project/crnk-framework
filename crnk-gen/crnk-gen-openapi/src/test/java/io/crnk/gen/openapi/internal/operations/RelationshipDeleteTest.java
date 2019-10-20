package io.crnk.gen.openapi.internal.operations;

import io.crnk.gen.openapi.internal.OperationType;
import io.swagger.v3.oas.models.Operation;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

class RelationshipDeleteTest extends NestedOperationsBaseTest {
  @Test
  void operationType() {
    RelationshipDelete RelationshipDelete = new RelationshipDelete(metaResource, metaResourceField, relatedMetaResource);
    Assert.assertEquals(OperationType.DELETE, RelationshipDelete.operationType());
  }

  @Test
  void isEnabledTrueWhenReadableAndFieldUpdatable() {
    RelationshipDelete RelationshipDelete = new RelationshipDelete(metaResource, metaResourceField, relatedMetaResource);
    Assert.assertTrue(metaResource.isReadable());
    metaResourceField.setUpdatable(true);
    Assert.assertTrue(RelationshipDelete.isEnabled());
  }

  @Test
  void isEnabledFalseWhenReadableAndFieldNotUpdatable() {
    RelationshipDelete RelationshipDelete = new RelationshipDelete(metaResource, metaResourceField, relatedMetaResource);
    Assert.assertTrue(metaResource.isReadable());
    metaResource.setUpdatable(false);
    Assert.assertFalse(RelationshipDelete.isEnabled());
  }

  @Test
  void isEnabledFalseWhenNotReadableAndFieldUpdatable() {
    RelationshipDelete RelationshipDelete = new RelationshipDelete(metaResource, metaResourceField, relatedMetaResource);
    metaResource.setReadable(false);
    metaResourceField.setUpdatable(true);
    Assert.assertFalse(RelationshipDelete.isEnabled());
  }

  @Test
  void isEnabledFalseWhenNotReadableAndFieldNotUpdatable() {
    RelationshipDelete RelationshipDelete = new RelationshipDelete(metaResource, metaResourceField, relatedMetaResource);
    metaResource.setReadable(false);
    metaResourceField.setUpdatable(false);
    Assert.assertFalse(RelationshipDelete.isEnabled());
  }

  @Test
  void getDescription() {
    RelationshipDelete RelationshipDelete = new RelationshipDelete(metaResource, metaResourceField, relatedMetaResource);
    Assert.assertEquals("Delete ResourceType relationship to a RelatedResourceType resource", RelationshipDelete.getDescription());
  }

  @Test
  void operation() {
    Operation operation = new RelationshipDelete(metaResource, metaResourceField, relatedMetaResource).operation();
    Assert.assertTrue(operation.getResponses().containsKey("200"));
  }

  @Test
  void path() {
    RelationshipDelete RelationshipDelete = new RelationshipDelete(metaResource, metaResourceField, relatedMetaResource);
    Assert.assertEquals("/ResourcePath/{id}/relationships/RelatedResourcePath", RelationshipDelete.path());
  }
}
