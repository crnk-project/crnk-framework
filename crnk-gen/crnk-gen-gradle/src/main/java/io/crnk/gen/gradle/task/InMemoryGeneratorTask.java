package io.crnk.gen.gradle.task;

import io.crnk.gen.base.GeneratorModule;
import io.crnk.gen.base.GeneratorConfig;
import io.crnk.gen.runtime.RuntimeContext;
import io.crnk.gen.gradle.internal.RuntimeClassLoaderFactory;
import io.crnk.gen.runtime.RuntimeMetaResolver;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.net.URLClassLoader;

public class InMemoryGeneratorTask extends DefaultTask implements GeneratorTaskContract {

    public static final String NAME = "generate";

    private GeneratorModule module;

    public InMemoryGeneratorTask() {
        setGroup("generation");
        setDescription("generate Typescript stubs from a Crnk setup");
    }

    @Internal
    public GeneratorModule getModule() {
        return module;
    }

    public void setModule(GeneratorModule module) {
        this.module = module;
    }

    @OutputDirectory
    public File getOutputDirectory() {
        return module.getGenDir();
    }

    @TaskAction
    public void generate() throws IOException {
        Thread thread = Thread.currentThread();
        ClassLoader contextClassLoader = thread.getContextClassLoader();

        RuntimeClassLoaderFactory classLoaderFactory = new RuntimeClassLoaderFactory(getProject(), module);

        GeneratorConfig config = getConfig();

        // no isolation needed when using classpath scanning
        boolean isolate = config.getResourcePackages() == null;
        URLClassLoader classloader = classLoaderFactory.createClassLoader(contextClassLoader, isolate);
        try {
            thread.setContextClassLoader(classloader);
            runGeneration(classloader);
        } finally {
            // make sure to restore the classloader when leaving this task
            thread.setContextClassLoader(contextClassLoader);

            // dispose classloader
            classloader.close();
        }
    }

    @Internal
    protected RuntimeMetaResolver getRuntime() {
        GeneratorConfig config = getConfig();
        String runtimeClass = config.computeMetaResolverClassName();
        return (RuntimeMetaResolver) loadClass(getClass().getClassLoader(), runtimeClass);
    }

    protected void runGeneration(ClassLoader classloader) {
        GeneratorConfig config = getConfig();

        module.setClassLoader(classloader);
        RuntimeContext context = new RuntimeContext(module, classloader, config);

        RuntimeMetaResolver runtime = getRuntime();
        runtime.run(context, classloader);
    }

    private Object loadClass(ClassLoader classLoader, String name) {
        try {
            Class<?> clazz = classLoader.loadClass(name);
            return clazz.newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new IllegalStateException("failed to load class", e);
        }
    }

    private GeneratorConfig getConfig() {
        Project project = getProject();
        return project.getExtensions().getByType(GeneratorConfig.class);
    }

}
