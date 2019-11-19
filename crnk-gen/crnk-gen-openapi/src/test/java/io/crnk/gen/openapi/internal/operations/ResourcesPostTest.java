package io.crnk.gen.openapi.internal.operations;

import io.crnk.gen.openapi.internal.OperationType;
import io.swagger.v3.oas.models.Operation;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

class ResourcesPostTest extends OperationsBaseTest {
  @Test
  void operationType() {
    ResourcesPost resourcePost = new ResourcesPost(metaResource);
    Assert.assertEquals(OperationType.POST, resourcePost.operationType());
  }

  @Test
  void isEnabledTrueWhenInsertable() {
    ResourcesPost resourcePost = new ResourcesPost(metaResource);
    Assert.assertTrue(metaResource.isInsertable());
    Assert.assertTrue(resourcePost.isEnabled());
  }

  @Test
  void isEnabled() {
    ResourcesPost resourcePost = new ResourcesPost(metaResource);
    metaResource.setInsertable(false);
    Assert.assertFalse(resourcePost.isEnabled());
  }

  @Test
  void getDescription() {
    ResourcesPost resourcePost = new ResourcesPost(metaResource);
    Assert.assertEquals("Create a ResourceName", resourcePost.getDescription());
  }

  @Test
  void getDescriptionWhenRepositoryIsBulk() {
    metaResource.getRepository().setBulk(true);
    ResourcesPost resourcePost = new ResourcesPost(metaResource);
    Assert.assertEquals("Create one (or more) ResourceName", resourcePost.getDescription());
  }

  @Test
  void operation() {
    Operation operation = new ResourcesPost(metaResource).operation();
    Assert.assertTrue(operation.getResponses().containsKey("201"));
  }

  @Test
  void path() {
    ResourcesPost resourcePost = new ResourcesPost(metaResource);
    Assert.assertEquals("/ResourcePath", resourcePost.path());
  }
}
