package io.crnk.gen.openapi;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.junit.Assert;

import java.util.List;

public class OpenAPIGeneratorTestBase {
  static void assertOperationResponseCodes(Operation operation, List<String> codes) {
    if (operation == null) {
      return;
    }
    ApiResponses responses = operation.getResponses();
    for (String code : codes) {
      Assert.assertTrue("Operation missing response code " + code, responses.containsKey(code));
    }
  }

}
