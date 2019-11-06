package io.crnk.gen.openapi.internal;

import io.crnk.gen.openapi.internal.schemas.ResponseMixin;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

public class OASErrorsTest {
  @Test
  public void test() {
    Map<String, ApiResponse> apiResponseCodes = OASErrors.generateStandardApiErrorResponses();

    for (Map.Entry<String, ApiResponse> entry : apiResponseCodes.entrySet()) {
      Assert.assertTrue(entry.getKey().startsWith("4") || entry.getKey().startsWith("5"));

      ApiResponse apiResponse = entry.getValue();
      Assert.assertNotNull(apiResponse.getDescription());

      Schema schema = apiResponse.getContent().get("application/vnd.api+json").getSchema();
      Assert.assertEquals(new ResponseMixin().$ref(), schema);
    }
  }
}
