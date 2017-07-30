package io.crnk.gen.typescript;

import io.crnk.gen.runtime.GeneratorTrigger;
import io.crnk.gen.runtime.RuntimeClassLoaderFactory;
import io.crnk.gen.typescript.internal.TSGeneratorRuntimeContext;
import io.crnk.gen.typescript.internal.TSGeneratorRuntimeContextImpl;
import io.crnk.gen.typescript.processor.TSSourceProcessor;
import io.crnk.gen.typescript.writer.TSCodeStyle;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.file.FileTree;
import org.gradle.api.tasks.*;

import java.io.File;
import java.io.IOException;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

public class GenerateTypescriptTask extends DefaultTask {

	public static final String NAME = "generateTypescript";

	public GenerateTypescriptTask() {
		setGroup("generation");
		setDescription("generate Typescript stubs from a Crnk setup");
	}

	/**
	 * Register resources directory as input to have incremental builds.
	 */
	@InputFiles
	public FileTree getResourcesInput() {
		return getMainSourceSet().getResources().getAsFileTree();
	}

	/**
	 * Register java sources as input to have incremental builds.
	 */
	@InputFiles
	@SkipWhenEmpty
	public FileTree getJavaInput() {
		return getMainSourceSet().getJava().getAsFileTree();
	}

	private SourceSet getMainSourceSet() {
		Project project = getProject();
		SourceSetContainer sourceSets = (SourceSetContainer) project.getProperties().get("sourceSets");
		return sourceSets.getByName("main");
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

		Map<String, Class<?>> sharedClasses = new HashMap<>();
		sharedClasses.put(GeneratorTrigger.class.getName(), GeneratorTrigger.class);
		sharedClasses.put(TSGeneratorConfiguration.class.getName(), TSGeneratorConfiguration.class);
		sharedClasses.put(TSNpmConfiguration.class.getName(), TSNpmConfiguration.class);
		sharedClasses.put(TSCodeStyle.class.getName(), TSCodeStyle.class);
		sharedClasses.put(RuntimeMetaResolver.class.getName(), RuntimeMetaResolver.class);
		sharedClasses.put(TSSourceProcessor.class.getName(), TSSourceProcessor.class);
		sharedClasses.put(TSGeneratorRuntimeContext.class.getName(), TSGeneratorRuntimeContext.class);

		RuntimeClassLoaderFactory classLoaderFactory = new RuntimeClassLoaderFactory(getProject());
		URLClassLoader classloader = classLoaderFactory.createClassLoader(contextClassLoader, sharedClasses);

		TSGeneratorConfiguration config = getConfig();
		setupDefaultConfig(config);

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
		try {
			Class<?> clazz = getClass().getClassLoader().loadClass(runtimeClass);
			return (RuntimeMetaResolver) clazz.newInstance();
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			throw new IllegalStateException("failed to load runtime " + runtimeClass, e);
		}
	}

	protected void runGeneration() {
		File outputDir = getOutputDirectory();
		TSGeneratorConfiguration config = getConfig();
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		try {
			Class<?> contextClass = classLoader.loadClass(TSGeneratorRuntimeContextImpl.class.getName());
			GeneratorTrigger context = (GeneratorTrigger) contextClass.newInstance();
			TSGeneratorRuntimeContext genContext = (TSGeneratorRuntimeContext) context;
			genContext.setOutputDir(outputDir);
			genContext.setConfig(config);
			RuntimeMetaResolver runtime = getRuntime();
			runtime.run(context, classLoader);
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			throw new IllegalStateException("failed to load class", e);
		}
	}

	private TSGeneratorConfiguration getConfig() {
		Project project = getProject();
		return project.getExtensions().getByType(TSGeneratorConfiguration.class);
	}
}
