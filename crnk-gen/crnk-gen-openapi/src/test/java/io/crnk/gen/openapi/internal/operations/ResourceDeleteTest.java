package io.crnk.gen.openapi.internal.operations;

import io.crnk.gen.openapi.internal.OperationType;
import io.swagger.v3.oas.models.Operation;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

class ResourceDeleteTest extends OperationsBaseTest {
  @Test
  void operationType() {
    ResourceDelete ResourceDelete = new ResourceDelete(metaResource);
    Assert.assertEquals(OperationType.DELETE, ResourceDelete.operationType());
  }

  @Test
  void isEnabledTrueWhenDeletable() {
    ResourceDelete ResourceDelete = new ResourceDelete(metaResource);
    Assert.assertTrue(metaResource.isDeletable());
    Assert.assertTrue(ResourceDelete.isEnabled());
  }

  @Test
  void isEnabled() {
    ResourceDelete ResourceDelete = new ResourceDelete(metaResource);
    metaResource.setDeletable(false);
    Assert.assertFalse(ResourceDelete.isEnabled());
  }

  @Test
  void getDescription() {
    ResourceDelete ResourceDelete = new ResourceDelete(metaResource);
    Assert.assertEquals("Delete a ResourceName", ResourceDelete.getDescription());
  }

  @Test
  void operation() {
    Operation operation = new ResourceDelete(metaResource).operation();
    Assert.assertTrue(operation.getResponses().containsKey("200"));
  }

  @Test
  void path() {
    ResourceDelete ResourceDelete = new ResourceDelete(metaResource);
    Assert.assertEquals("/ResourcePath/{id}", ResourceDelete.path());
  }
}
