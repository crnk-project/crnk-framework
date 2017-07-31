package io.crnk.gen.typescript;

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

import javax.naming.Context;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;

public class GenerateTypescriptTaskTest {

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

		TSGeneratorConfiguration config = project.getExtensions().getByType(TSGeneratorConfiguration.class);
		config.setExpressionLibrary("@crnk/ngrx");
		config.setGenerateExpressions(expressions);
		String testPackage = "@crnk/gen-typescript-test";
		config.getRuntime().setConfiguration("test");
		config.getNpm().setPackageName(testPackage);
		config.getNpm().setGitRepository("someThing");
		config.getNpm().getPackageMapping().put("io.crnk.test.mock.models", testPackage);
		config.getNpm().getPackageMapping().put("io.crnk.meta", testPackage);
		config.getNpm().setPackageVersion("0.0.1");

		GenerateTypescriptTask task = (GenerateTypescriptTask) project.getTasks().getByName("generateTypescript");
		task.runGeneration();

		Copy processTask = (Copy) project.getTasks().getByName("processTypescript");
		processTask.execute();

		assertExists("build/generated/source/typescript/package.json");
		assertExists("build/generated/source/typescript/src/index.ts");
		assertExists("build/generated/source/typescript/src/project.ts");
		assertExists("build/generated/source/typescript/src/project.data.ts");
		assertExists("build/generated/source/typescript/src/schedule.ts");
		assertExists("build/generated/source/typescript/src/task.ts");
		assertNotExists("build/generated/source/typescript/src/task.links.ts");
		assertNotExists("build/generated/source/typescript/src/task.meta.ts");

		assertExists("build/generated/source/typescript/src/meta.key.ts");
		assertExists("build/generated/source/typescript/src/meta.element.ts");
		assertExists("build/generated/source/typescript/src/meta.data.object.ts");

		// check whether source copied to compile directory for proper source bundling
		assertExists("build/npm_compile/.npmrc");
		assertExists("build/npm_compile/package.json");
		assertExists("build/npm_compile/src/index.ts");
		assertExists("build/npm_compile/src/meta.element.ts");

		Charset utf8 = Charset.forName("UTF8");
		String expectedSourceFileName = expressions ? "expected_schedule_with_expressions.ts" :
				"expected_schedule_without_expressions.ts";
		String expectedSource = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(expectedSourceFileName), utf8);
		String actualSource = IOUtils
				.toString(new FileInputStream(new File(outputDir, "build/generated/source/typescript/src/schedule.ts")), utf8);
		Assert.assertEquals(expectedSource, actualSource);
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
