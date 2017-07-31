package io.crnk.gen.typescript;

import io.crnk.gen.runtime.GeneratorTrigger;
import io.crnk.gen.runtime.RuntimeClassLoaderFactory;
import io.crnk.gen.typescript.internal.TSGeneratorRuntimeContext;
import io.crnk.gen.typescript.internal.TSGeneratorRuntimeContextImpl;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.net.URLClassLoader;

public class GenerateTypescriptTask extends DefaultTask {

	public static final String NAME = "generateTypescript";

	public GenerateTypescriptTask() {
		setGroup("generation");
		setDescription("generate Typescript stubs from a Crnk setup");
	}

	public File getOutputDirectory() {
		Project project = getProject();
		String srcDirectoryPath = "generated/source/typescript/";
		return new File(project.getBuildDir(), srcDirectoryPath);
	}

	@TaskAction
	public void generate() throws IOException {
		Thread thread = Thread.currentThread();
		ClassLoader contextClassLoader = thread.getContextClassLoader();

		RuntimeClassLoaderFactory classLoaderFactory = new RuntimeClassLoaderFactory(getProject());
		URLClassLoader classloader = classLoaderFactory.createClassLoader(contextClassLoader);
		try {
			thread.setContextClassLoader(classloader);

			runGeneration();
		} finally {
			// make sure to restore the classloader when leaving this task
			thread.setContextClassLoader(contextClassLoader);

			// dispose classloader
			classloader.close();
		}

	}

	protected void setupDefaultConfig(TSGeneratorConfiguration config) {
		String defaultVersion = getProject().getVersion().toString();
		if (config.getNpm().getPackageVersion() == null) {
			config.getNpm().setPackageVersion(defaultVersion);
		}
	}

	protected RuntimeMetaResolver getRuntime() {
		TSGeneratorConfiguration config = getConfig();
		String runtimeClass = config.getMetaResolverClassName();
		return (RuntimeMetaResolver) loadClass(getClass().getClassLoader(), runtimeClass);
	}

	protected void runGeneration() {
		TSGeneratorConfiguration config = getConfig();
		setupDefaultConfig(config);

		File outputDir = getOutputDirectory();
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

		GeneratorTrigger context = (GeneratorTrigger) loadClass(classLoader, TSGeneratorRuntimeContextImpl.class.getName());
		TSGeneratorRuntimeContext genContext = (TSGeneratorRuntimeContext) context;
		genContext.setOutputDir(outputDir);
		genContext.setConfig(config);
		RuntimeMetaResolver runtime = getRuntime();
		runtime.run(context, classLoader);


	}

	private Object loadClass(ClassLoader classLoader, String name) {
		try {
			Class<?> clazz = classLoader.loadClass(name);
			return clazz.newInstance();
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			throw new IllegalStateException("failed to load class", e);
		}
	}

	private TSGeneratorConfiguration getConfig() {
		Project project = getProject();
		return project.getExtensions().getByType(TSGeneratorConfiguration.class);
	}

}
