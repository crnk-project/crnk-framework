package io.crnk.gen.typescript;

import io.crnk.gen.base.GeneratorModule;
import io.crnk.gen.base.GeneratorModuleConfigBase;
import io.crnk.gen.typescript.internal.TSGenerator;
import io.crnk.gen.typescript.internal.TSGeneratorRuntimeContext;
import io.crnk.gen.typescript.model.writer.TSCodeStyle;
import io.crnk.gen.typescript.processor.TSSourceProcessor;
import io.crnk.meta.MetaLookupImpl;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;


public class TSGeneratorModule implements GeneratorModule, TSGeneratorRuntimeContext {

    public static final String NAME = "typescript";

    private TSGeneratorConfig config = new TSGeneratorConfig();

    private ClassLoader classloader;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void generate(Object meta) throws IOException {
        MetaLookupImpl metaLookup = (MetaLookupImpl) meta;

        TSGenerator gen = new TSGenerator(config.getGenDir(), metaLookup, config);
        gen.run();
    }

    @Override
    public ClassLoader getClassLoader() {
        if (classloader == null) {
            throw new IllegalStateException();
        }
        return classloader;
    }

    @Override
    public void setClassLoader(ClassLoader classloader) {
        this.classloader = classloader;
    }

    public TSGeneratorConfig getConfig() {
        return config;
    }

    @Override
    public void initDefaults(File buildDir) {
        config.setBuildDir(buildDir);
        config.getExcludes().add("resources.meta");
        config.getNpm().setPackageName("@packageNameNotSpecified");
    }

    @Override
    public File getGenDir() {
        return config.getGenDir();
    }

    @Override
    public Collection<Class> getConfigClasses() {
        return Arrays.asList(TSCodeStyle.class, TSResourceFormat.class, TSGeneratorConfig.class,
                TSNpmConfiguration.class, TSSourceProcessor.class);
    }

    @Override
    public void setConfig(GeneratorModuleConfigBase config) {
        this.config = (TSGeneratorConfig) config;
    }


}