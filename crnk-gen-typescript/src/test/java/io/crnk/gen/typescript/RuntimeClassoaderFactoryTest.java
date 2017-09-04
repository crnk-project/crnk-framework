package io.crnk.gen.typescript;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import javax.naming.Context;

import io.crnk.core.engine.document.Resource;
import io.crnk.gen.runtime.RuntimeClassLoaderFactory;
import io.crnk.gen.typescript.model.TSClassType;
import io.crnk.gen.typescript.model.TSImport;
import io.crnk.gen.typescript.model.TSMember;
import io.crnk.gen.typescript.runtime.DummyInitialContextFactory;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class RuntimeClassoaderFactoryTest {

	@Rule
	public TemporaryFolder testProjectDir = new TemporaryFolder();

	private RuntimeClassLoaderFactory.SharedClassLoader sharedClassLoader;

	private URLClassLoader classLoader;
	private RuntimeClassLoaderFactory factory;


	@Before
	public void setup() throws IOException {
		// Deltaspike sometimes really wants to have a retarded JNDI context
		System.setProperty(Context.INITIAL_CONTEXT_FACTORY, DummyInitialContextFactory.class.getName());

		testProjectDir.newFolder("src", "main", "java");

		File outputDir = testProjectDir.getRoot();

		Project project = ProjectBuilder.builder().withName("crnk-gen-typescript-test").withProjectDir(outputDir).build();
		project.setVersion("0.0.1");

		project.getPluginManager().apply("com.moowork.node");
		project.getPluginManager().apply(JavaPlugin.class);
		project.getPluginManager().apply(TSGeneratorPlugin.class);

		TSGeneratorExtension config = project.getExtensions().getByType(TSGeneratorExtension.class);
		config.getRuntime().setConfiguration("test");

		factory = new RuntimeClassLoaderFactory(project);
		ClassLoader parentClassLoader = getClass().getClassLoader();
		classLoader = factory.createClassLoader(parentClassLoader);
		sharedClassLoader = (RuntimeClassLoaderFactory.SharedClassLoader) classLoader.getParent();

	}

	@Test
	public void checkSharedClassAccessible() throws IOException, ClassNotFoundException {
		sharedClassLoader.putSharedClass("test", String.class);

		Assert.assertSame(String.class, classLoader.loadClass("test"));
		Assert.assertSame(String.class, sharedClassLoader.loadClass("test"));
	}

	@Test
	public void checkBootstrapClassesAccessible() throws IOException, ClassNotFoundException {
		Assert.assertSame(String.class, classLoader.loadClass(String.class.getName()));
		Assert.assertSame(Object.class, classLoader.loadClass(Object.class.getName()));
	}


	@Test
	public void checkTypescriptModelExposed() throws IOException, ClassNotFoundException {
		Assert.assertSame(TSMember.class, classLoader.loadClass(TSMember.class.getName()));
		Assert.assertSame(TSClassType.class, classLoader.loadClass(TSClassType.class.getName()));
		Assert.assertSame(TSImport.class, classLoader.loadClass(TSImport.class.getName()));
	}

	@Test
	public void defaultLogbackTestProvidedFromParentClassloader() throws IOException, ClassNotFoundException {
		Assert.assertNotNull(classLoader.getResource("logback-test.xml"));
	}

	@Test(expected = IllegalStateException.class)
	public void defaultLogbackTestNotProvidedWithInvalidParentClassLoader() throws IOException, ClassNotFoundException {
		ClassLoader bootstrapClassLoader = ClassLoader.getSystemClassLoader().getParent();
		classLoader = factory.createClassLoader(bootstrapClassLoader);
		Assert.assertNull(bootstrapClassLoader.getResource("logback-test.xml"));
		Assert.assertNull(classLoader.getResource("logback-test.xml"));
	}

	@Test
	public void classLoaderExposesTestConfiguration() throws IOException, ClassNotFoundException {
		Assert.assertNotEquals(0, classLoader.getURLs().length);
	}

	@Test(expected = ClassNotFoundException.class)
	public void classLoaderIsolatesCallerEnvironment() throws IOException, ClassNotFoundException {
		// methods from context classpath should not be accessible
		classLoader.loadClass(Resource.class.getName());
	}

}
