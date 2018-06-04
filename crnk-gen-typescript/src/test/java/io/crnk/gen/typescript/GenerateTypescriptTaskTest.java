package io.crnk.gen.typescript;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import javax.naming.Context;

import io.crnk.gen.typescript.runtime.DummyInitialContextFactory;
import org.apache.commons.io.IOUtils;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.Copy;
import org.gradle.internal.impldep.org.junit.Assert;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GenerateTypescriptTaskTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(GenerateTypescriptTaskTest.class);

	@Rule
	public TemporaryFolder testProjectDir = new TemporaryFolder();

	private File outputDir;


	@Test
	public void testWithExpressions() throws IOException {
		test(true);
	}

	@Test
	public void testWithoutExpressions() throws IOException {
		test(false);
	}

	private void test(boolean expressions) throws IOException {
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
		String testPackage = "@crnk/gen-typescript-test";
		config.getRuntime().setConfiguration("test");
		config.getNpm().setPackagingEnabled(true);
		config.getNpm().setPackageName(testPackage);
		config.getNpm().setGitRepository("someThing");
		config.getNpm().getPackageMapping().put("io.crnk.test.mock.models", testPackage);
		config.getNpm().getPackageMapping().put("io.crnk.meta", testPackage);
		config.getNpm().setPackageVersion("0.0.1");

		TSGeneratorPlugin plugin = project.getPlugins().getPlugin(TSGeneratorPlugin.class);
		plugin.init(project);

		GenerateTypescriptTask task = (GenerateTypescriptTask) project.getTasks().getByName("generateTypescript");
		task.runGeneration(Thread.currentThread().getContextClassLoader());

		Copy processTask = (Copy) project.getTasks().getByName("processTypescript");
		processTask.execute();

		assertExists("build/generated/source/typescript/package.json");
		assertExists("build/generated/source/typescript/src/index.ts");
		assertExists("build/generated/source/typescript/src/projects.ts");
		assertExists("build/generated/source/typescript/src/types/project.data.ts");
		assertExists("build/generated/source/typescript/src/schedule.ts");
		assertExists("build/generated/source/typescript/src/tasks.ts");
		assertNotExists("build/generated/source/typescript/src/tasks.links.ts");
		assertNotExists("build/generated/source/typescript/src/tasks.meta.ts");

		assertExists("build/generated/source/typescript/src/meta/meta.key.ts");
		assertExists("build/generated/source/typescript/src/meta/meta.element.ts");
		assertExists("build/generated/source/typescript/src/meta/meta.data.object.ts");

		// check whether source copied to compile directory for proper source bundling
		assertExists("build/npm_compile/.npmrc");
		assertExists("build/npm_compile/package.json");
		assertExists("build/npm_compile/src/index.ts");
		assertExists("build/npm_compile/src/meta/meta.element.ts");

		checkSchedule(expressions);
		checkProject();
		if (expressions) {
			checkProjectData();
		}
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

	private void checkSchedule(boolean expressions) throws IOException {
		String expectedSourceFileName = expressions ? "expected_schedule_with_expressions.ts" :
				"expected_schedule_without_expressions.ts";
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

		LOGGER.info(actualSource);

		System.out.println(actualSource);

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
