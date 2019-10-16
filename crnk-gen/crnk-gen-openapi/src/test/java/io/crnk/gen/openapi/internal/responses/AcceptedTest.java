package io.crnk.gen.openapi.internal.responses;

import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
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
    Assert.assertTrue(schema instanceof ObjectSchema);
    Assert.assertEquals(1, schema.getProperties().size());
    Schema idSchema = (Schema) schema.getProperties().get("id");
    Assert.assertTrue(idSchema instanceof StringSchema);
    Assert.assertEquals("a unique identifier for this pending action", idSchema.getDescription());
  }
}
