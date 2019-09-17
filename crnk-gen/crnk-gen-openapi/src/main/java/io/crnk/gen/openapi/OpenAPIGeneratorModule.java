package io.crnk.gen.openapi;

import io.crnk.gen.base.GeneratorModule;
import io.crnk.gen.base.GeneratorModuleConfigBase;
import io.crnk.gen.openapi.internal.OASGenerator;
import io.crnk.meta.MetaLookup;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.models.OpenAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;


public class OpenAPIGeneratorModule implements GeneratorModule {

  private static final Logger LOGGER = LoggerFactory.getLogger(OpenAPIGeneratorModule.class);
  private static final String NAME = "openapi";
  private OpenAPIGeneratorConfig config = new OpenAPIGeneratorConfig();
  private ClassLoader classloader;

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public void generate(Object meta) throws IOException {
    LOGGER.info("performing openapi generation");
    OASGenerator oasGenerator = new OASGenerator((MetaLookup) meta, config.getOpenAPI());
    oasGenerator.buildPaths();
    OpenAPI openApi = oasGenerator.getOpenApi();
    write("openapi", Yaml.pretty(openApi));
  }

  @Override
  public ClassLoader getClassLoader() {
    return classloader;
  }

  @Override
  public void setClassLoader(ClassLoader classloader) {
    this.classloader = classloader;
  }

  @Override
  public void initDefaults(File buildDir) {
    config.setBuildDir(buildDir);
  }

  @Override
  public File getGenDir() {
    return config.getGenDir();
  }

  @Override
  public Collection<Class> getConfigClasses() {
    return Collections.singletonList(OpenAPIGeneratorConfig.class);
  }

  public OpenAPIGeneratorConfig getConfig() {
    return config;
  }

  @Override
  public void setConfig(GeneratorModuleConfigBase config) {
    this.config = (OpenAPIGeneratorConfig) config;
  }

  private File write(String fileName, String source) throws IOException {
    File file = new File(config.getGenDir(), fileName + ".yaml");
    file.getParentFile().mkdirs();
    try (FileWriter writer = new FileWriter(file)) {
      writer.write(source);
    }
    return file;
  }
}
