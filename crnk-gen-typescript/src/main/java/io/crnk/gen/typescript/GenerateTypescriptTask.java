package io.crnk.gen.typescript;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import io.crnk.gen.typescript.model.TSElement;
import io.crnk.gen.typescript.processor.TSSourceProcessor;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.file.FileTree;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.SkipWhenEmpty;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskAction;

import io.crnk.gen.typescript.writer.TSCodeStyle;

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

	@OutputDirectory
	public File getOutputDirectory() {
		Project project = getProject();
		String srcDirectoryPath = "generated/source/typescript/";
		return new File(project.getBuildDir(), srcDirectoryPath);
	}

	@TaskAction
	public void generate() throws IOException {
		Thread thread = Thread.currentThread();
		ClassLoader contextClassLoader = thread.getContextClassLoader();
		ClassLoader classloader = this.getProjectClassLoader(contextClassLoader);

		try {
			thread.setContextClassLoader(classloader);

			runGeneration();
		}
		finally {
			// make sure to restore the classloader when leaving this task
			thread.setContextClassLoader(contextClassLoader);
		}

	}

	protected RuntimeIntegration getRuntime() {
		TSGeneratorConfiguration config = getConfig();
		String runtimeClass = config.getRuntimeClassName();
		try {
			Class<?> clazz = getClass().getClassLoader().loadClass(runtimeClass);
			return (RuntimeIntegration) clazz.newInstance();
		}
		catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			throw new IllegalStateException("failed to load runtime " + runtimeClass, e);
		}
	}

	protected void runGeneration() {
		File outputDir = getOutputDirectory();
		TSGeneratorConfiguration config = getConfig();
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

		RuntimeIntegration runtime = getRuntime();
		runtime.run(outputDir, config, classLoader);
	}

	private TSGeneratorConfiguration getConfig() {
		Project project = getProject();
		return project.getExtensions().getByType(TSGeneratorConfiguration.class);
	}

	private Set<File> getProjectClassFiles() {
		Set<File> classpath = new HashSet<>();

		Project project = getProject();
		SourceSetContainer sourceSets = (SourceSetContainer) project.getProperties().get("sourceSets");

		SortedSet<String> availableSourceSetNames = sourceSets.getNames();
		for (String sourceSetName : Arrays.asList("main", "test", "integrationTest")) {
			if (availableSourceSetNames.contains(sourceSetName)) {
				SourceSet sourceSet = sourceSets.getByName(sourceSetName);
				classpath.add(sourceSet.getOutput().getClassesDir());
			}
		}

		// add gradle integrationTest dependencies to url
		org.gradle.api.artifacts.Configuration runtimeConfiguration = project.getConfigurations()
				.getByName("integrationTestRuntime");
		classpath.addAll(runtimeConfiguration.getFiles());

		return classpath;
	}

	/**
	 * Build a classloader so we setup a CDI container, get Crnk and do the generation.
	 */
	public ClassLoader getProjectClassLoader(ClassLoader parentClassLoader) {
		Set<URL> classURLs = new HashSet<>(); // NOSONAR URL needed by URLClassLoader
		classURLs.addAll(getProjectClassUrls());
		classURLs.add(getPluginUrl());

		// do not expose the gradle classpath, so we use the bootstrap classloader instead
		ClassLoader bootstrapClassLaoder = ClassLoader.getSystemClassLoader().getParent();

		// some classes still need to be shared between plugin and generation
		ClassLoader sharedClassLoader = getSharedClassLoader(bootstrapClassLaoder, parentClassLoader);

		return new URLClassLoader(classURLs.toArray(new URL[0]), sharedClassLoader);
	}

	private static ClassLoader getSharedClassLoader(ClassLoader bootstrapClassLaoder, final ClassLoader parentClassLoader) {
		final Map<String, Class<?>> sharedClasses = new HashMap<>();
		sharedClasses.put(TSGeneratorConfiguration.class.getName(), TSGeneratorConfiguration.class);
		sharedClasses.put(TSCodeStyle.class.getName(), TSCodeStyle.class);
		sharedClasses.put(RuntimeIntegration.class.getName(), RuntimeIntegration.class);
		sharedClasses.put(TSSourceProcessor.class.getName(), TSSourceProcessor.class);
		return new ClassLoader(bootstrapClassLaoder) {

			@Override
			protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
				// share typescript model (since transformation can be defined externally)
				if (name.startsWith(TSElement.class.getPackage().getName())) {
					return parentClassLoader.loadClass(name);
				}

				if (sharedClasses.containsKey(name)) {
					return sharedClasses.get(name);
				}
				return super.loadClass(name, resolve);
			}
		};
	}

	private URL getPluginUrl() {
		// add this plugin itself to the runtime classpath to make integration available
		URLClassLoader classLoader = (URLClassLoader) getClass().getClassLoader();
		for (URL gradleClassUrl : classLoader.getURLs()) {
			if (gradleClassUrl.getFile().contains("moap-typescript")) {
				return gradleClassUrl;
			}
		}
		throw new IllegalStateException("moap-typescript plugin jar not found");
	}

	private Collection<? extends URL> getProjectClassUrls() {
		Set<File> projectClassFiles = getProjectClassFiles();
		Collection<URL> urls = new ArrayList<>();
		for(File file : projectClassFiles){
			try {
				urls.add(file.toURI().toURL());
			}
			catch (MalformedURLException e) {
				throw new IllegalStateException();
			}
		}
		return urls;
	}

}
