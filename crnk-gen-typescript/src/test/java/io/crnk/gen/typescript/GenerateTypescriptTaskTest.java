package io.crnk.gen.typescript;

import io.crnk.gen.typescript.runtime.DummyInitialContextFactory;
import org.apache.commons.io.IOUtils;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.AbstractCopyTask;
import org.gradle.api.tasks.Copy;
import org.gradle.internal.impldep.org.junit.Assert;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.Context;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;

public class GenerateTypescriptTaskTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(GenerateTypescriptTaskTest.class);

    @Rule
    public TemporaryFolder testProjectDir = new TemporaryFolder();

    private File outputDir;

    @Test
    public void testWithExpressions() throws IOException {
        test(true, TSResourceFormat.JSONAPI);
    }

    @Test
    public void testWithoutExpressions() throws IOException {
        test(false, TSResourceFormat.JSONAPI);
    }

    @Test
    public void testPlainJson() throws IOException {
        test(false, TSResourceFormat.PLAINJSON);
    }


    private void test(boolean expressions, TSResourceFormat resourceFormat) throws IOException {
        // Deltaspike sometimes really wants to have a retarded JNDI context
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, DummyInitialContextFactory.class.getName());

        testProjectDir.newFolder("src", "main", "java");

        outputDir = testProjectDir.getRoot();
        outputDir = new File("build/tmp/gen");
        outputDir.mkdirs();

        File npmrcFile = new File(outputDir, ".npmrc");
        FileWriter npmrcWriter = new FileWriter(npmrcFile);
        npmrcWriter.write("");
        npmrcWriter.close();

        Project project = ProjectBuilder.builder().withName("crnk-gen-typescript-test").withProjectDir(outputDir).build();
        project.setVersion("0.0.1");

        project.getPluginManager().apply("com.moowork.node");
        project.getPluginManager().apply(JavaPlugin.class);
        project.getPluginManager().apply(TSGeneratorPlugin.class);

        TSGeneratorExtension config = project.getExtensions().getByType(TSGeneratorExtension.class);
        config.setForked(false);
        config.setGenerateExpressions(expressions);
        configure(config);
        String testPackage = "@crnk/gen-typescript-test";
        config.getNpm().setPackagingEnabled(true);
        config.getNpm().setPackageName(testPackage);
        config.getNpm().setGitRepository("someThing");
        config.getNpm().getPackageMapping().put("io.crnk.test.mock.models", testPackage);
        config.getNpm().getPackageMapping().put("io.crnk.meta", testPackage);
        config.getNpm().setPackageVersion("0.0.1");
        config.setFormat(resourceFormat);

        TSGeneratorPlugin plugin = project.getPlugins().getPlugin(TSGeneratorPlugin.class);
        plugin.init(project);

        GenerateTypescriptTask task = (GenerateTypescriptTask) project.getTasks().getByName("generateTypescript");
        task.runGeneration(Thread.currentThread().getContextClassLoader());

        Copy processTask = (Copy) project.getTasks().getByName("processTypescript");
        try {
            Method method = AbstractCopyTask.class.getDeclaredMethod("copy");
            method.setAccessible(true);
            method.invoke(processTask);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        assertExists("build/generated/source/typescript/package.json");
        assertExists("build/generated/source/typescript/src/index.ts");
        assertExists("build/generated/source/typescript/src/projects.ts");
        assertExists("build/generated/source/typescript/src/types/project.data.ts");
        assertExists("build/generated/source/typescript/src/schedule.ts");
        assertExists("build/generated/source/typescript/src/tasks.ts");
        if (resourceFormat == TSResourceFormat.PLAINJSON) {
            assertExists("build/generated/source/typescript/src/crnk.ts");
        }
        assertNotExists("build/generated/source/typescript/src/tasks.links.ts");
        assertNotExists("build/generated/source/typescript/src/tasks.meta.ts");

        // check whether source copied to compile directory for proper source bundling
        assertExists("build/npm_compile/.npmrc");
        assertExists("build/npm_compile/package.json");
        assertExists("build/npm_compile/src/index.ts");

        checkSchedule(expressions, resourceFormat);
        checkProject();
        if (expressions) {
            checkProjectData();
        }
    }

    protected void configure(TSGeneratorExtension config) {
        config.getRuntime().setConfiguration("test");
    }

    private void checkProjectData() throws IOException {
        String expectedSourceFileName = "expected_project_data.ts";
        String actualSourcePath = "build/generated/source/typescript/src/types/project.data.ts";
        compare(expectedSourceFileName, actualSourcePath);
    }

    private void checkProject() throws IOException {
        Charset utf8 = Charset.forName("UTF8");
        String actualSource = IOUtils
                .toString(new FileInputStream(new File(outputDir, "build/generated/source/typescript/src/projects.ts")), utf8);
        Assert.assertTrue(actualSource.contains(" from './types/project.data'"));
    }

    private void checkSchedule(boolean expressions, TSResourceFormat format) throws IOException {
        String expectedSourceFileName;
        if (format == TSResourceFormat.PLAINJSON) {
            expectedSourceFileName = "expected_schedule_plain_json.ts";
        } else if (expressions) {
            expectedSourceFileName = "expected_schedule_with_expressions.ts";
        } else {
            expectedSourceFileName = "expected_schedule_without_expressions.ts";
        }

        String actualSourcePath = "build/generated/source/typescript/src/schedule.ts";
        compare(expectedSourceFileName, actualSourcePath);
    }

    private void compare(String expectedSourceFileName, String actualSourcePath) throws IOException {
        Charset utf8 = Charset.forName("UTF8");

        String expectedSource = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(expectedSourceFileName), utf8);
        String actualSource = IOUtils
                .toString(new FileInputStream(new File(outputDir, actualSourcePath)), utf8);

        expectedSource = expectedSource.replace("\r\n", "\n");

        LoggerFactory.getLogger(getClass()).info(actualSource);

        System.out.println(actualSource);

        LOGGER.info(actualSource);

        String[] expectedLines = org.apache.commons.lang3.StringUtils.split(expectedSource, '\n');
        String[] actualLines = org.apache.commons.lang3.StringUtils.split(actualSource, '\n');
        for (int i = 0; i < expectedLines.length; i++) {
            Assert.assertEquals("line: " + Integer.toString(i) + ", " + expectedLines[i], expectedLines[i], actualLines[i]);
        }
        Assert.assertEquals(expectedLines.length, actualLines.length);
    }

    private void assertExists(String path) {
        File file = new File(outputDir, path);
        Assert.assertTrue(file.getAbsolutePath(), file.exists());
    }

    private void assertNotExists(String path) {
        File file = new File(outputDir, path);
        Assert.assertFalse(file.getAbsolutePath(), file.exists());
    }
}
