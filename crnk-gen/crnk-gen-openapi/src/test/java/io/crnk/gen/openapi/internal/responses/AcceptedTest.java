package io.crnk.gen.openapi.internal.responses;

import io.crnk.gen.openapi.internal.schemas.Success;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

class AcceptedTest {

  @Test
  void response() {
    ApiResponse apiResponse = new Accepted().response();
    Assert.assertEquals("Accepted", apiResponse.getDescription());
    Content content = apiResponse.getContent();
    Assert.assertEquals(1, content.size());
    Schema schema = content.get("application/vnd.api+json").getSchema();
    Assert.assertEquals(new Success().$ref().get$ref(), schema.get$ref());
  }
}
