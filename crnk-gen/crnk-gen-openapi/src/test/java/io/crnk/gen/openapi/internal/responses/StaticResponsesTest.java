package io.crnk.gen.openapi.internal.responses;

import io.swagger.v3.oas.models.responses.ApiResponse;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.util.Map;

class StaticResponsesTest {

  @Test
  void generateStandardApiResponses() {
    Map<String, ApiResponse> apiResponseMap = StaticResponses.generateStandardApiResponses();
    Assert.assertEquals(16, apiResponseMap.size());
    Assert.assertNotNull(apiResponseMap.get("NoContent"));
    Assert.assertNotNull(apiResponseMap.get("400"));
    Assert.assertNotNull(apiResponseMap.get("401"));
    Assert.assertNotNull(apiResponseMap.get("403"));
    Assert.assertNotNull(apiResponseMap.get("404"));
    Assert.assertNotNull(apiResponseMap.get("405"));
    Assert.assertNotNull(apiResponseMap.get("409"));
    Assert.assertNotNull(apiResponseMap.get("412"));
    Assert.assertNotNull(apiResponseMap.get("415"));
    Assert.assertNotNull(apiResponseMap.get("422"));
    Assert.assertNotNull(apiResponseMap.get("500"));
    Assert.assertNotNull(apiResponseMap.get("501"));
    Assert.assertNotNull(apiResponseMap.get("502"));
    Assert.assertNotNull(apiResponseMap.get("503"));
    Assert.assertNotNull(apiResponseMap.get("504"));
    Assert.assertNotNull(apiResponseMap.get("505"));
  }
}
