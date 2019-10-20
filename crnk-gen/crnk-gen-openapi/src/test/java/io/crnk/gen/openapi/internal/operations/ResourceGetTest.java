package io.crnk.gen.openapi.internal.operations;

import io.crnk.gen.openapi.internal.OperationType;
import io.swagger.v3.oas.models.Operation;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

class ResourceGetTest extends OperationsBaseTest {
  @Test
  void operationType() {
    ResourceGet resourcePost = new ResourceGet(metaResource);
    Assert.assertEquals(OperationType.GET, resourcePost.operationType());
  }

  @Test
  void isEnabledTrueWhenReadable() {
    ResourceGet resourcePost = new ResourceGet(metaResource);
    Assert.assertTrue(metaResource.isReadable());
    Assert.assertTrue(resourcePost.isEnabled());
  }

  @Test
  void isEnabled() {
    ResourceGet resourcePost = new ResourceGet(metaResource);
    metaResource.setReadable(false);
    Assert.assertFalse(resourcePost.isEnabled());
  }

  @Test
  void getDescription() {
    ResourceGet resourcePost = new ResourceGet(metaResource);
    Assert.assertEquals("Retrieve a ResourceType resource", resourcePost.getDescription());
  }

  @Test
  void operation() {
    Operation operation = new ResourceGet(metaResource).operation();
    Assert.assertTrue(operation.getResponses().containsKey("200"));
  }

  @Test
  void path() {
    ResourceGet resourcePost = new ResourceGet(metaResource);
    Assert.assertEquals("/ResourcePath/{id}", resourcePost.path());
  }
}
