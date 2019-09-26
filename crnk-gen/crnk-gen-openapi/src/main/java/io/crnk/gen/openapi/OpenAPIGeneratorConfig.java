package io.crnk.gen.openapi;

import io.crnk.gen.base.GeneratorModuleConfigBase;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.parser.OpenAPIV3Parser;

import java.io.File;

public class OpenAPIGeneratorConfig extends GeneratorModuleConfigBase {

  private File genDir = null;

  private File buildDir = null;

  private String templateName = null;

  private OpenAPI openAPI = null;

  /**
   * @return location where the generated sources are placed.
   */
  public File getGenDir() {
    if (genDir == null) {
      return new File(buildDir, "generated/source/openapi/");
    }
    return genDir;
  }

  public void setGenDir(File genDir) {
    this.genDir = genDir;
  }

  public File getBuildDir() {
    return buildDir;
  }

  public void setBuildDir(File buildDir) {
    this.buildDir = buildDir;
  }

  /**
   * @return the name of the template used as a base to merge generated openapi into.
   */
  private String getTemplateName() {
    return templateName;
  }

  public void setTemplateName(String templateName) {
    this.templateName = templateName;
  }

  /**
   * @return a starter openapi object that includes templated objects if a template was specified.
   */
  public OpenAPI getOpenAPI() {
    if (openAPI == null) {
      if (this.getTemplateName() != null) {
        File templateFile = new File(buildDir, this.getTemplateName());
        OpenAPI openAPI = new OpenAPIV3Parser().read(templateFile.getAbsolutePath());

        if (openAPI.getPaths() == null) {
          openAPI.paths(new Paths());
        }

        if (openAPI.getComponents() == null) {
          openAPI.components(new Components());
        }

        return openAPI;
      }

      return new OpenAPI()
          .info(new Info()
              .description("Generated Description")
              .title("Generated Title")
              .version("0.1.0")
          )
          .paths(new Paths())
          .components(new Components());
    }
    return openAPI;
  }

  public void setOpenAPI(OpenAPI openAPI) {
    this.openAPI = openAPI;
  }
}
