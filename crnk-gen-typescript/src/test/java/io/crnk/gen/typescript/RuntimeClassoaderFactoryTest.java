package io.crnk.gen.typescript;

import io.crnk.gen.runtime.RuntimeClassLoaderFactory;
import io.crnk.gen.typescript.runtime.DummyInitialContextFactory;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import javax.naming.Context;
import java.io.File;
import java.io.IOException;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

public class RuntimeClassoaderFactoryTest {

	@Rule
	public TemporaryFolder testProjectDir = new TemporaryFolder();


	@Test
	public void test() throws IOException, ClassNotFoundException {
		// Deltaspike sometimes really wants to have a retarded JNDI context
		System.setProperty(Context.INITIAL_CONTEXT_FACTORY, DummyInitialContextFactory.class.getName());

		testProjectDir.newFolder("src", "main", "java");

		File outputDir = testProjectDir.getRoot();

		Project project = ProjectBuilder.builder().withName("crnk-gen-typescript-test").withProjectDir(outputDir).build();
		project.setVersion("0.0.1");
		project.getPluginManager().apply(JavaPlugin.class);

		Map<String, Class<?>> sharedClasses = new HashMap<>();
		sharedClasses.put("test", String.class);

		RuntimeClassLoaderFactory factory = new RuntimeClassLoaderFactory(project);
		ClassLoader parentClassLoader = ClassLoader.getSystemClassLoader();
		URLClassLoader classLoader = factory.createClassLoader(parentClassLoader, sharedClasses);
		Assert.assertNotEquals(0, classLoader.getURLs().length);

		// check shared class loading
		Assert.assertSame(String.class, classLoader.loadClass("test"));

		// check logback-test.xml
		Assert.assertNotNull(classLoader.getResource("logback-test.xml"));

	}

}
