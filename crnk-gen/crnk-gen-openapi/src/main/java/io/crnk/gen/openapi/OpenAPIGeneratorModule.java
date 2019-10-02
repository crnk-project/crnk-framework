package io.crnk.gen.openapi;

import io.crnk.gen.base.GeneratorModule;
import io.crnk.gen.base.GeneratorModuleConfigBase;
import io.crnk.gen.openapi.internal.OASGenerator;
import io.crnk.meta.MetaLookup;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;


public class OpenAPIGeneratorModule implements GeneratorModule {

  private static final String NAME = "openapi";

  private OpenAPIGeneratorConfig config = new OpenAPIGeneratorConfig();

  private ClassLoader classloader;

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public void generate(Object meta) throws IOException {
    OASGenerator gen = new OASGenerator(config.getGenDir(), (MetaLookup) meta, config);
    gen.run();
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
}
