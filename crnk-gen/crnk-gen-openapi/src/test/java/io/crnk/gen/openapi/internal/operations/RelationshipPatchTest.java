package io.crnk.gen.openapi.internal.operations;

import io.crnk.gen.openapi.internal.OperationType;
import io.swagger.v3.oas.models.Operation;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

class RelationshipPatchTest extends NestedOperationsBaseTest {
  @Test
  void operationType() {
    RelationshipPatch RelationshipPatch = new RelationshipPatch(metaResource, metaResourceField, relatedMetaResource);
    Assert.assertEquals(OperationType.PATCH, RelationshipPatch.operationType());
  }

  @Test
  void isEnabledTrueWhenReadableAndFieldUpdatable() {
    RelationshipPatch RelationshipPatch = new RelationshipPatch(metaResource, metaResourceField, relatedMetaResource);
    Assert.assertTrue(metaResource.isReadable());
    metaResourceField.setUpdatable(true);
    Assert.assertTrue(RelationshipPatch.isEnabled());
  }

  @Test
  void isEnabledFalseWhenReadableAndFieldNotUpdatable() {
    RelationshipPatch RelationshipPatch = new RelationshipPatch(metaResource, metaResourceField, relatedMetaResource);
    Assert.assertTrue(metaResource.isReadable());
    metaResource.setUpdatable(false);
    Assert.assertFalse(RelationshipPatch.isEnabled());
  }

  @Test
  void isEnabledFalseWhenNotReadableAndFieldUpdatable() {
    RelationshipPatch RelationshipPatch = new RelationshipPatch(metaResource, metaResourceField, relatedMetaResource);
    metaResource.setReadable(false);
    metaResourceField.setUpdatable(true);
    Assert.assertFalse(RelationshipPatch.isEnabled());
  }

  @Test
  void isEnabledFalseWhenNotReadableAndFieldNotUpdatable() {
    RelationshipPatch RelationshipPatch = new RelationshipPatch(metaResource, metaResourceField, relatedMetaResource);
    metaResource.setReadable(false);
    metaResourceField.setUpdatable(false);
    Assert.assertFalse(RelationshipPatch.isEnabled());
  }

  @Test
  void getDescription() {
    RelationshipPatch RelationshipPatch = new RelationshipPatch(metaResource, metaResourceField, relatedMetaResource);
    Assert.assertEquals("Update ResourceType relationship to a RelatedResourceType resource", RelationshipPatch.getDescription());
  }

  @Test
  void operation() {
    Operation operation = new RelationshipPatch(metaResource, metaResourceField, relatedMetaResource).operation();
    Assert.assertTrue(operation.getResponses().containsKey("200"));
  }

  @Test
  void path() {
    RelationshipPatch RelationshipPatch = new RelationshipPatch(metaResource, metaResourceField, relatedMetaResource);
    Assert.assertEquals("/ResourcePath/{id}/relationships/someRelatedResource", RelationshipPatch.path());
  }
}
