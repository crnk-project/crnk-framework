package io.crnk.gen.openapi;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;

class OpenAPIGeneratorTestBase {
  @Before
  public void resetYamlSingleton() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
     Field instance = Yaml.class.getDeclaredField("mapper");
     instance.setAccessible(true);
     instance.set(null, null);
  }

  static void assertJsonAPICompliantPath(String path, PathItem pathItem) {
    if (path.contains("}/relationships")) {
      assertJsonAPICompliantRelationshipsPath(pathItem);
    } else if (path.contains("}/")) {
      assertJsonAPICompliantNestedPath(pathItem);
    } else if (path.endsWith("}")) {
      assertJsonAPICompliantResourcePath(pathItem);
    } else if (!path.contains("}")) {
      assertJsonAPICompliantResourcesPath(pathItem);
    }
  }

  static void assertResponsesSorted(PathItem pathItem) {
    for (Operation operation : pathItem.readOperationsMap().values()) {
      List<String> responses = new ArrayList<>(operation.getResponses().keySet());
      List<String> sorted = new ArrayList<>(responses);
      Collections.sort(sorted);
      Assert.assertEquals("Responses should be sorted.", sorted, responses);
    }
  }

  void compare(String expectedSourceFileName, String actualSourcePath) throws IOException {
    Charset utf8 = Charset.forName("UTF8");

    String expectedSource;
    try (InputStream in = getClass().getClassLoader().getResourceAsStream(expectedSourceFileName)) {
      expectedSource = IOUtils.toString(in, utf8);
    }

    String actualSource;
    try (FileInputStream in = new FileInputStream(new File(actualSourcePath))) {
      actualSource = IOUtils.toString(in, utf8);
    }

    expectedSource = expectedSource.replace("\r\n", "\n");

    String[] expectedLines = org.apache.commons.lang3.StringUtils.split(expectedSource, '\n');
    String[] actualLines = org.apache.commons.lang3.StringUtils.split(actualSource, '\n');
    for (int i = 0; i < expectedLines.length; i++) {
      Assert.assertEquals("line: " + i + ", " + expectedLines[i], expectedLines[i], actualLines[i]);
    }
    Assert.assertEquals(expectedLines.length, actualLines.length);
  }

  private static void assertOperationResponseCodes(Operation operation, List<String> codes) {
    // Some operations will be null in cases where a JsonApiResource has postable, patchable,
    // readable, deleteable set to false. In that case, do not perform any response checks
    if(operation == null) { return; }

    ApiResponses responses = operation.getResponses();
    for (String code : codes) {
      Assert.assertTrue("Operation missing response code " + code, responses.containsKey(code));
    }
  }

  private static void assertJsonAPICompliantResourcePath(PathItem pathItem) {
    // Ensure GET response json:api compliance
    assertOperationResponseCodes(pathItem.getGet(), Arrays.asList("200", "400"));

    // Ensure PATCH response json:api compliance
    assertOperationResponseCodes(pathItem.getPatch(), Arrays.asList("200", "202", "204", "403", "404", "409"));

    // Ensure DELETE response json:api compliance
    assertOperationResponseCodes(pathItem.getDelete(), Arrays.asList("200", "202", "204", "404"));
  }

  private static void assertJsonAPICompliantResourcesPath(PathItem pathItem) {
    // Ensure GET response json:api compliance
    assertOperationResponseCodes(pathItem.getGet(), Arrays.asList("200", "400"));

    // Ensure POST response json:api compliance
    assertOperationResponseCodes(pathItem.getPost(), Arrays.asList("201", "202", "204", "403", "404", "409"));

  }

  private static void assertJsonAPICompliantNestedPath(PathItem pathItem) {
    // Ensure GET response json:api compliance
    assertOperationResponseCodes(pathItem.getGet(), Arrays.asList("200", "400"));

    // Ensure POST response json:api compliance
    assertOperationResponseCodes(pathItem.getPost(), Arrays.asList("200", "202", "204", "403", "404", "409"));

    // Ensure PATCH response json:api compliance
    assertOperationResponseCodes(pathItem.getPatch(), Arrays.asList("200", "202", "204", "403", "404", "409"));

    // Ensure DELETE response json:api compliance
    assertOperationResponseCodes(pathItem.getDelete(), Arrays.asList("200", "202", "204", "404"));
  }

  private static void assertJsonAPICompliantRelationshipsPath(PathItem pathItem) {
    // Ensure GET response json:api compliance
    assertOperationResponseCodes(pathItem.getGet(), Arrays.asList("200", "400"));

    // Ensure POST response json:api compliance
    assertOperationResponseCodes(pathItem.getPost(), Arrays.asList("200", "202", "204", "403", "404", "409"));

    // Ensure PATCH response json:api compliance
    assertOperationResponseCodes(pathItem.getPatch(), Arrays.asList("200", "202", "204", "403", "404", "409"));

    // Ensure DELETE response json:api compliance
    assertOperationResponseCodes(pathItem.getDelete(), Arrays.asList("200", "202", "204", "404"));
  }
}
