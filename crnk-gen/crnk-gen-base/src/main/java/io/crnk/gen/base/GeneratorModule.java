package io.crnk.gen.base;


import java.io.File;
import java.io.IOException;
import java.util.Collection;

public interface GeneratorModule {

    String getName();

    /**
     * Triggers the generation of the provided meta-data. To be invoked by RuntimeIntegration once ready.
     *
     * @param lookup of type io.katharsis.meta.MetaLookup. Not that the MetaLookup class is not available here.
     */
    void generate(Object lookup) throws IOException;

    ClassLoader getClassLoader();

    void setClassLoader(ClassLoader classloader);

    GeneratorModuleConfigBase getConfig();

    void initDefaults(File buildDir);

    File getGenDir();

    Collection<Class> getConfigClasses();

    void setConfig(GeneratorModuleConfigBase moduleConfig);
}