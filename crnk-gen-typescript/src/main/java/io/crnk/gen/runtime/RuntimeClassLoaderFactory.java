package io.crnk.gen.runtime;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import io.crnk.gen.typescript.GenerateTypescriptTask;
import io.crnk.gen.typescript.model.TSElement;
import org.gradle.api.Project;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;

/**
 * Code generation runs within the application classpath, not in the gradle classpath.
 * This factory constructs such a classloader. Currently it makes use of the integrationTest
 * classpath and Deltaspike, future versions may improve upon that resp. generalize it to
 * other environments like Spring.
 */
public class RuntimeClassLoaderFactory {

	private Project project;

	public RuntimeClassLoaderFactory(Project project) {
		this.project = project;
	}

	public ClassLoader createClassLoader(ClassLoader parentClassLoader, Map<String, Class<?>> sharedClasses) {
		Set<URL> classURLs = new HashSet<>(); // NOSONAR URL needed by URLClassLoader
		classURLs.addAll(getProjectClassUrls());
		classURLs.add(getPluginUrl());

		// do not expose the gradle classpath, so we use the bootstrap classloader instead
		ClassLoader bootstrapClassLaoder = ClassLoader.getSystemClassLoader().getParent();

		// some classes still need to be shared between plugin and generation
		ClassLoader sharedClassLoader = new SharedClassLoader(bootstrapClassLaoder, parentClassLoader, sharedClasses);

		return new URLClassLoader(classURLs.toArray(new URL[0]), sharedClassLoader);
	}

	protected class SharedClassLoader extends ClassLoader {

		private ClassLoader parentClassLoader;

		private Map<String, Class<?>> sharedClasses;

		public SharedClassLoader(ClassLoader bootstrapClassLoader, ClassLoader parentClassLoader,
				Map<String, Class<?>> sharedClasses) {
			super(bootstrapClassLoader);
			this.parentClassLoader = parentClassLoader;
			this.sharedClasses = sharedClasses;
		}

		@Override
		protected synchronized URL findResource(String name) {
			if ("logback-test.xml".equals(name)) {
				URL logbackUrl = RuntimeClassLoaderFactory.class.getClassLoader().getResource("logback-test.xml");
				if (logbackUrl == null) {
					throw new IllegalStateException("logback-test.xml could not be found");
				}
				return logbackUrl;
			}
			URL sharedResourceUrl = GenerateTypescriptTask.class.getClassLoader().getResource(name);
			if (sharedResourceUrl != null) {
				return sharedResourceUrl;
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

	}

	private URL getPluginUrl() {
		// add this plugin itself to the runtime classpath to make integration available
		URLClassLoader classLoader = (URLClassLoader) getClass().getClassLoader();
		for (URL gradleClassUrl : classLoader.getURLs()) {
			if (gradleClassUrl.getFile().contains("crnk-gen-typescript")) {
				return gradleClassUrl;
			}
		}
		throw new IllegalStateException("crnk-gen-typescript.jar not found in gradle build classpath");
	}

	private Set<File> getProjectClassFiles() {
		Set<File> classpath = new HashSet<>();

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

	private Collection<? extends URL> getProjectClassUrls() {
		Set<File> projectClassFiles = getProjectClassFiles();
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