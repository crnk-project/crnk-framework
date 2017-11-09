package io.crnk.gen.runtime;

import java.io.File;
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

import io.crnk.gen.typescript.RuntimeMetaResolver;
import io.crnk.gen.typescript.TSGeneratorConfig;
import io.crnk.gen.typescript.TSNpmConfiguration;
import io.crnk.gen.typescript.TSRuntimeConfiguration;
import io.crnk.gen.typescript.internal.TSGeneratorRuntimeContext;
import io.crnk.gen.typescript.model.TSElement;
import io.crnk.gen.typescript.processor.TSSourceProcessor;
import io.crnk.gen.typescript.writer.TSCodeStyle;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Code generation runs within the application classpath, not in the gradle classpath.
 * This factory constructs such a classloader. Currently it makes use of the integrationTest
 * classpath and Deltaspike, future versions may improve upon that resp. generalize it to
 * other environments like Spring.
 */
public class RuntimeClassLoaderFactory {

	private static final Logger LOGGER = LoggerFactory.getLogger(RuntimeClassLoaderFactory.class);

	private Project project;

	public RuntimeClassLoaderFactory(Project project) {
		this.project = project;
	}

	public URLClassLoader createClassLoader(ClassLoader parentClassLoader) {
		Set<URL> classURLs = new HashSet<>(); // NOSONAR URL needed by URLClassLoader
		classURLs.addAll(getProjectLibraryUrls());
		classURLs.add(getPluginUrl());

		// do not expose the gradle classpath, so we use the bootstrap classloader instead
		ClassLoader bootstrapClassLaoder = ClassLoader.getSystemClassLoader().getParent();

		// some classes still need to be shared between plugin and generation
		ClassLoader sharedClassLoader = new SharedClassLoader(bootstrapClassLaoder, parentClassLoader);

		return new URLClassLoader(classURLs.toArray(new URL[0]), sharedClassLoader);
	}


	public static class SharedClassLoader extends ClassLoader {

		private ClassLoader parentClassLoader;

		private Map<String, Class<?>> sharedClasses;

		public SharedClassLoader(ClassLoader bootstrapClassLoader, ClassLoader parentClassLoader) {
			super(bootstrapClassLoader);
			this.parentClassLoader = parentClassLoader;

			sharedClasses = new HashMap<>();
			sharedClasses.put(GeneratorTrigger.class.getName(), GeneratorTrigger.class);
			sharedClasses.put(TSGeneratorConfig.class.getName(), TSGeneratorConfig.class);
			sharedClasses.put(TSNpmConfiguration.class.getName(), TSNpmConfiguration.class);
			sharedClasses.put(TSRuntimeConfiguration.class.getName(), TSRuntimeConfiguration.class);
			sharedClasses.put(TSCodeStyle.class.getName(), TSCodeStyle.class);
			sharedClasses.put(io.crnk.gen.typescript.RuntimeMetaResolver.class.getName(), RuntimeMetaResolver.class);
			sharedClasses.put(TSSourceProcessor.class.getName(), TSSourceProcessor.class);
			sharedClasses.put(TSGeneratorRuntimeContext.class.getName(), TSGeneratorRuntimeContext.class);
		}

		@Override
		protected synchronized URL findResource(String name) {
			if ("logback-test.xml".equals(name)) {
				URL logbackUrl = parentClassLoader.getResource("logback-test.xml");
				if (logbackUrl == null) {
					throw new IllegalStateException("logback-test.xml could not be found");
				}
				return logbackUrl;
			}
			return super.findResource(name);
		}

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

		public void putSharedClass(String name, Class<?> clazz) {
			sharedClasses.put(name, clazz);
		}
	}

	public URL getPluginUrl() {
		// add this plugin itself to the runtime classpath to make integration available
		URLClassLoader classLoader = (URLClassLoader) getClass().getClassLoader();
		for (URL gradleClassUrl : classLoader.getURLs()) {
			if (gradleClassUrl.getFile().contains("crnk-gen-typescript")) {
				return gradleClassUrl;
			}
		}
		throw new IllegalStateException("crnk-gen-typescript.jar not found in gradle build classpath");
	}

	public Set<File> getProjectLibraries() {
		Set<File> classpath = new HashSet<>();

		SourceSetContainer sourceSets = (SourceSetContainer) project.getProperties().get("sourceSets");

		SortedSet<String> availableSourceSetNames = sourceSets.getNames();
		for (String sourceSetName : Arrays.asList("main", "test", "integrationTest")) {
			if (availableSourceSetNames.contains(sourceSetName)) {
				SourceSet sourceSet = sourceSets.getByName(sourceSetName);
				classpath.add(sourceSet.getOutput().getClassesDir());
			}
		}

		// add  dependencies from configured gradle configuration to url (usually test or integrationTest)
		TSGeneratorConfig generatorConfiguration = project.getExtensions().getByType(TSGeneratorConfig.class);
		String configurationName = generatorConfiguration.getRuntime().getConfiguration();

		ConfigurationContainer configurations = project.getConfigurations();
		Configuration runtimeConfiguration = configurations.findByName(configurationName + "Runtime");
		if (runtimeConfiguration == null) {
			runtimeConfiguration = configurations.getByName(configurationName);
		}
		classpath.addAll(runtimeConfiguration.getFiles());

		for (File file : classpath) {
			LOGGER.debug("classpath entry: {}", file);
		}

		return classpath;
	}

	private Collection<? extends URL> getProjectLibraryUrls() {
		Set<File> projectClassFiles = getProjectLibraries();
		Collection<URL> urls = new ArrayList<>();
		for (File file : projectClassFiles) {
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