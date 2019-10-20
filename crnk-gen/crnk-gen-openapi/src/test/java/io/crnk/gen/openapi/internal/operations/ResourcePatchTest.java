package io.crnk.gen.openapi.internal.operations;

import io.crnk.gen.openapi.internal.OperationType;
import io.swagger.v3.oas.models.Operation;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

class ResourcePatchTest extends OperationsBaseTest {
  @Test
  void operationType() {
    ResourcePatch resourcePost = new ResourcePatch(metaResource);
    Assert.assertEquals(OperationType.PATCH, resourcePost.operationType());
  }

  @Test
  void isEnabledTrueWhenUpdateable() {
    ResourcePatch resourcePost = new ResourcePatch(metaResource);
    Assert.assertTrue(metaResource.isUpdatable());
    Assert.assertTrue(resourcePost.isEnabled());
  }

  @Test
  void isEnabled() {
    ResourcePatch resourcePost = new ResourcePatch(metaResource);
    metaResource.setUpdatable(false);
    Assert.assertFalse(resourcePost.isEnabled());
  }

  @Test
  void getDescription() {
    ResourcePatch resourcePost = new ResourcePatch(metaResource);
    Assert.assertEquals("Update a ResourceName", resourcePost.getDescription());
  }

  @Test
  void operation() {
    Operation operation = new ResourcePatch(metaResource).operation();
    Assert.assertTrue(operation.getResponses().containsKey("200"));
  }

  @Test
  void path() {
    ResourcePatch resourcePost = new ResourcePatch(metaResource);
    Assert.assertEquals("/ResourcePath/{id}", resourcePost.path());
  }
}
