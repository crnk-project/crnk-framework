package io.crnk.gen.gradle;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import io.crnk.gen.gradle.task.InMemoryGeneratorTask;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class GeneratorPluginTest {

    @Rule
    public TemporaryFolder testProjectDir = new TemporaryFolder();

    private GeneratorExtension extension;

    private InMemoryGeneratorTask task;


    @Before
    public void setup() throws IOException {
    	GeneratorPlugin.APPLY_DOCLET_BY_DEFAULT = false;

        testProjectDir.newFolder("src", "main", "java");

        File outputDir = testProjectDir.getRoot();

        Project project = ProjectBuilder.builder().withName("crnk-gen-typescript-test").withProjectDir(outputDir).build();
        project.setVersion("0.0.1");

        project.getPluginManager().apply(JavaPlugin.class);
        project.getPluginManager().apply(GeneratorPlugin.class);

        extension = project.getExtensions().getByType(GeneratorExtension.class);
        extension.getRuntime().setConfiguration("test");
        extension.setResourcePackages(Arrays.asList("io.crnk.test.mock"));
        extension.setForked(false);
        extension.init();

        task = (InMemoryGeneratorTask) project.getTasks().getByName("generateTypescript");
        Assert.assertNotNull(task);


    }

    @Test
    public void checkGenerate() throws IOException {
        task.generate();

        File genDir = new File(testProjectDir.getRoot(), "build/generated/sources/typescript");
        Assert.assertTrue(genDir.exists());
        Assert.assertTrue(new File(genDir, "projects.ts").exists());
    }
}
