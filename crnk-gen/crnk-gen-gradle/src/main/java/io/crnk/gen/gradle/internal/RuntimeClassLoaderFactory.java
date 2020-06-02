package io.crnk.gen.gradle.internal;

import io.crnk.gen.base.GeneratorConfig;
import io.crnk.gen.base.GeneratorModule;
import io.crnk.gen.base.RuntimeConfiguration;
import io.crnk.gen.base.SpringRuntimeConfig;
import io.crnk.gen.gradle.GeneratorExtension;
import io.crnk.gen.runtime.RuntimeContext;
import io.crnk.gen.runtime.RuntimeMetaResolver;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

/**
 * Code generation runs within the application classpath, not in the gradle classpath.
 * This factory constructs such a classloader. Currently it makes use of the integrationTest
 * classpath and Deltaspike, future versions may improve upon that resp. generalize it to
 * other environments like Spring.
 */
public class RuntimeClassLoaderFactory {

	private static final Logger LOGGER = LoggerFactory.getLogger(RuntimeClassLoaderFactory.class);

	private final GeneratorModule module;

	private Project project;

	public RuntimeClassLoaderFactory(Project project, GeneratorModule module) {
		this.project = project;
		this.module = module;
	}

	public URLClassLoader createClassLoader(ClassLoader parentClassLoader, boolean isolate) {
		Set<URL> classURLs = new HashSet<>(); // NOSONAR URL needed by URLClassLoader
		classURLs.addAll(getProjectLibraryUrls());
		classURLs.addAll(getPluginUrls());

		// some classes still need to be shared between plugin and generation
		ClassLoader sharedClassLoader;
		if (isolate) {
			// do not expose the gradle classpath, so we use the bootstrap classloader instead
			ClassLoader bootstrapClassLaoder = ClassLoader.getSystemClassLoader().getParent();

			sharedClassLoader = new SharedClassLoader(bootstrapClassLaoder, parentClassLoader);
		} else {
			sharedClassLoader = parentClassLoader;
		}

		return new URLClassLoader(classURLs.toArray(new URL[0]), sharedClassLoader);
	}


	public class SharedClassLoader extends ClassLoader {

		private ClassLoader parentClassLoader;

		private Map<String, Class<?>> sharedClasses;

		public SharedClassLoader(ClassLoader bootstrapClassLoader, ClassLoader parentClassLoader) {
			super(bootstrapClassLoader);
			this.parentClassLoader = parentClassLoader;

			sharedClasses = new HashMap<>();
			sharedClasses.put(GeneratorConfig.class.getName(), GeneratorConfig.class);

			sharedClasses.put(SpringRuntimeConfig.class.getName(), SpringRuntimeConfig.class);
			sharedClasses.put(RuntimeConfiguration.class.getName(), RuntimeConfiguration.class);
			sharedClasses.put(RuntimeMetaResolver.class.getName(), RuntimeMetaResolver.class);
			sharedClasses.put(RuntimeContext.class.getName(), RuntimeContext.class);

			module.getConfigClasses().forEach(it -> sharedClasses.put(it.getName(), it));
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
			if (name.startsWith("crnk-") && name.endsWith(".ts")) {
				URL logbackUrl = parentClassLoader.getResource(name);
				if (logbackUrl == null) {
					throw new IllegalStateException(name + " could not be found in typescript generator");
				}
				return logbackUrl;
			}
			return super.findResource(name);
		}

		@Override
		protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
			// share typescript model (since transformation can be defined externally)
			if (name.startsWith("io.crnk.gen.typescript.model")) {
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

	public List<URL> getPluginUrls() {
		List<URL> urls = new ArrayList<>();

		// add this plugin itself to the runtime classpath to make integration available
		URLClassLoader classLoader = (URLClassLoader) getClass().getClassLoader();
		for (URL gradleClassUrl : classLoader.getURLs()) {
			String file = gradleClassUrl.getFile();

            /*
            |         +--- org.webjars.npm:viz.js:2.1.2
            |         +--- org.apache.xmlgraphics:xmlgraphics-commons:2.3
            |         |    \--- commons-io:commons-io:1.3.1 -> 1.3.2
            |         +--- commons-io:commons-io:2.6 -> 1.3.2
            |         +--- guru.nidi.com.kitfox:svgSalamander:1.1.2
            |         +--- net.arnx:nashorn-promise:0.1.1
            |         +--- com.eclipsesource.j2v8:j2v8_macosx_x86_64:4.6.0
            |         +--- com.eclipsesource.j2v8:j2v8_linux_x86_64:4.6.0
            |         +--- com.eclipsesource.j2v8:j2v8_win32_x86_64:4.6.0
            |         +--- com.eclipsesource.j2v8:j2v8_win32_x86:4.6.0
            |         +--- org.apache.commons:commons-exec:1.3
            |         +--- com.google.code.findbugs:jsr305:3.0.2
            |         +--- org.slf4j:jcl-over-slf4j:1.7.25
            |         |    \--- org.slf4j:slf4j-api:1.7.25
            |         +--- org.slf4j:jul-to-slf4j:1.7.25 (*)
            |         \--- org.slf4j:slf4j-api:1.7.25
            */
			if (file.contains("v8") || file.contains("webjars") || file.contains("xmlgraphics")
					|| file.contains("nashorn") || file.contains("svg") || file.contains("305")
					|| file.contains("commons") || file.contains("graphviz") || file.contains("crnk-gen-")
					|| file.contains("reflections") || file.contains("crnk-meta-")
					|| file.contains("crnk-data-jpa-")) {
				urls.add(gradleClassUrl);
			}
		}
		if (urls.isEmpty()) {
			throw new IllegalStateException("crnk-gen-gradle.jar not found in gradle build classpath");
		}
		return urls;
	}

	public Set<File> getProjectLibraries() {
		Set<File> classpath = new HashSet<>();

		SourceSetContainer sourceSets = (SourceSetContainer) project.getProperties().get("sourceSets");

		if (sourceSets != null) {
			SortedSet<String> availableSourceSetNames = sourceSets.getNames();
			for (String sourceSetName : Arrays.asList("main", "test", "integrationTest")) {
				if (availableSourceSetNames.contains(sourceSetName)) {
					SourceSet sourceSet = sourceSets.getByName(sourceSetName);
					classpath.addAll(sourceSet.getOutput().getClassesDirs().getFiles());
					classpath.add(sourceSet.getOutput().getResourcesDir());
				}
			}
		}

		// add  dependencies from configured gradle configuration to url (usually test or integrationTest)
		GeneratorConfig generatorConfiguration = project.getExtensions().getByType(GeneratorExtension.class);
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
			} catch (MalformedURLException e) {
				throw new IllegalStateException();
			}
		}
		return urls;
	}
}