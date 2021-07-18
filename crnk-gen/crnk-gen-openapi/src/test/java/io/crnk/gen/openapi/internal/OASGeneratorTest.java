package io.crnk.gen.openapi.internal;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Objects;

import io.crnk.gen.openapi.OutputFormat;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class OASGeneratorTest {

  private OpenAPI openApi;

  @Before
  public void setup() {
    String templatePath = Objects.requireNonNull(
        getClass().getClassLoader().getResource("openapi-template.yml")
    ).getPath();
    if(SystemUtils.IS_OS_WINDOWS) {
      templatePath = StringUtils.removeStart(templatePath, "/");
    }
    openApi = new OpenAPIV3Parser().read(templatePath);
  }

  @Test
  public void testGenerateOpenApiContentGeneratesYamlUnsorted() throws IOException {

    compare(
        "gold/unsorted.yaml",
        OASGenerator.generateOpenApiContent(openApi, OutputFormat.YAML, false));
  }

  @Test
  public void testGenerateOpenApiContentGeneratesYamlSorted() throws IOException {
    compare(
        "gold/sorted.yaml",
        OASGenerator.generateOpenApiContent(openApi, OutputFormat.YAML, true));
  }

  @Test
  public void testGenerateOpenApiContentGeneratesJsonUnsorted() throws IOException {
    compare(
        "gold/unsorted.json",
        OASGenerator.generateOpenApiContent(openApi, OutputFormat.JSON, false));
  }

  @Test
  public void testGenerateOpenApiContentGeneratesJsonSorted() throws IOException {
    compare(
        "gold/sorted.json",
        OASGenerator.generateOpenApiContent(openApi, OutputFormat.JSON, true));
  }

  private void compare(String expectedSourceFileName, String actualSource) throws IOException {
    Charset utf8 = Charset.forName("UTF8");

    String expectedSource;
    try (InputStream in = getClass().getClassLoader().getResourceAsStream(expectedSourceFileName)) {
      expectedSource = IOUtils.toString(in, utf8);
    }

    expectedSource = expectedSource.replace("\r\n", "\n");
	actualSource = actualSource.replace("\r\n", "\n"); // TODO maybe update the generator part?

    String[] expectedLines = org.apache.commons.lang3.StringUtils.split(expectedSource, '\n');
    String[] actualLines = org.apache.commons.lang3.StringUtils.split(actualSource, '\n');
    for (int i = 0; i < expectedLines.length; i++) {
      Assert.assertEquals("line: " + i + ", " + expectedLines[i], expectedLines[i], actualLines[i]);
    }
    Assert.assertEquals(expectedLines.length, actualLines.length);
  }
}
