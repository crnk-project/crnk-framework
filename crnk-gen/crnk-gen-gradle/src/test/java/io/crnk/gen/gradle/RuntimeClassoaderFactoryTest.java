package io.crnk.gen.gradle;

import io.crnk.core.engine.document.Resource;
import io.crnk.gen.gradle.internal.RuntimeClassLoaderFactory;
import io.crnk.gen.typescript.TSGeneratorModule;
import io.crnk.gen.typescript.model.TSClassType;
import io.crnk.gen.typescript.model.TSImport;
import io.crnk.gen.typescript.model.TSMember;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.net.URLClassLoader;

public class RuntimeClassoaderFactoryTest {

    @Rule
    public TemporaryFolder testProjectDir = new TemporaryFolder();

    private RuntimeClassLoaderFactory.SharedClassLoader sharedClassLoader;

    private URLClassLoader classLoader;

    private RuntimeClassLoaderFactory factory;


    @Before
    public void setup() throws IOException {
        testProjectDir.newFolder("src", "main", "java");

        File outputDir = testProjectDir.getRoot();

        Project project = ProjectBuilder.builder().withName("crnk-gen-typescript-test").withProjectDir(outputDir).build();
        project.setVersion("0.0.1");

        project.getPluginManager().apply(JavaPlugin.class);
        project.getPluginManager().apply(GeneratorPlugin.class);

        GeneratorExtension config = project.getExtensions().getByType(GeneratorExtension.class);
        config.getRuntime().setConfiguration("test");

        TSGeneratorModule module = new TSGeneratorModule();

        factory = new RuntimeClassLoaderFactory(project, module);
        ClassLoader parentClassLoader = getClass().getClassLoader();
        classLoader = factory.createClassLoader(parentClassLoader, true);
        sharedClassLoader = (RuntimeClassLoaderFactory.SharedClassLoader) classLoader.getParent();

    }

    @Test
    public void checkSharedClassAccessible() throws ClassNotFoundException {
        sharedClassLoader.putSharedClass("test", String.class);

        Assert.assertSame(String.class, classLoader.loadClass("test"));
        Assert.assertSame(String.class, sharedClassLoader.loadClass("test"));
    }

    @Test
    public void checkBootstrapClassesAccessible() throws ClassNotFoundException {
        Assert.assertSame(String.class, classLoader.loadClass(String.class.getName()));
        Assert.assertSame(Object.class, classLoader.loadClass(Object.class.getName()));
    }


    @Test
    public void checkTypescriptModelExposed() throws ClassNotFoundException {
        Assert.assertSame(TSMember.class, classLoader.loadClass(TSMember.class.getName()));
        Assert.assertSame(TSClassType.class, classLoader.loadClass(TSClassType.class.getName()));
        Assert.assertSame(TSImport.class, classLoader.loadClass(TSImport.class.getName()));
    }

    @Test
    public void defaultLogbackTestProvidedFromParentClassloader() {
        Assert.assertNotNull(classLoader.getResource("logback-test.xml"));
    }

    @Test(expected = IllegalStateException.class)
    public void defaultLogbackTestNotProvidedWithInvalidParentClassLoader() {
        ClassLoader bootstrapClassLoader = ClassLoader.getSystemClassLoader().getParent();
        classLoader = factory.createClassLoader(bootstrapClassLoader, true);
        Assert.assertNull(bootstrapClassLoader.getResource("logback-test.xml"));
        Assert.assertNull(classLoader.getResource("logback-test.xml"));
    }

    @Test
    public void classLoaderExposesTestConfiguration() {
        Assert.assertNotEquals(0, classLoader.getURLs().length);
    }

    @Test(expected = ClassNotFoundException.class)
    public void classLoaderIsolatesCallerEnvironment() throws ClassNotFoundException {
        // methods from context classpath should not be accessible
        classLoader.loadClass(Resource.class.getName());
    }

}
