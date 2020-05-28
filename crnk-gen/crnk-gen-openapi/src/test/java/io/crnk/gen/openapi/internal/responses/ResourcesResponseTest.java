package io.crnk.gen.openapi.internal.responses;

import io.crnk.gen.openapi.internal.MetaResourceBaseTest;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

class ResourcesResponseTest extends MetaResourceBaseTest {

  @Test
  void response() {
    ApiResponse apiResponse = new ResourcesResponse(metaResource).response();
    Assert.assertNotNull(apiResponse);
    Assert.assertEquals("OK", apiResponse.getDescription());
    Content content = apiResponse.getContent();
    Assert.assertEquals(1, content.size());
    Schema schema = content.get("application/vnd.api+json").getSchema();
    Assert.assertEquals(
        "#/components/schemas/ResourceTypeResourcesResponseSchema",
        schema.get$ref()
    );
  }
}