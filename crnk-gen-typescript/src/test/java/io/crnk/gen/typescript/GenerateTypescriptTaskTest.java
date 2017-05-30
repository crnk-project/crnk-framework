package io.crnk.gen.typescript;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import javax.naming.Context;

import org.apache.commons.io.IOUtils;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.internal.impldep.org.junit.Assert;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

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
		System.setProperty(Context.INITIAL_CONTEXT_FACTORY, io.crnk.gen.typescript.DummyInitialContextFactory.class.getName());

		testProjectDir.newFolder("src", "main", "java");

		outputDir = testProjectDir.getRoot();

		//	outputDir = new File("c:/projects/temp");

		Project project = ProjectBuilder.builder().withProjectDir(outputDir).build();

		project.getPluginManager().apply(JavaPlugin.class);
		project.getPluginManager().apply(TSGeneratorPlugin.class);

		TSGeneratorConfiguration config = project.getExtensions().getByType(TSGeneratorConfiguration.class);
		config.setGenerateExpressions(expressions);
		config.setNpmPackageName("@crnk/gen-typescript-test");
		config.getNpmPackageMapping().put("io.crnk.test.mock.models", "@crnk/gen-typescript-test");
		config.getNpmPackageMapping().put("io.crnk.meta", "@crnk/meta");

		GenerateTypescriptTask task = (GenerateTypescriptTask) project.getTasks().getByName("generateTypescript");
		task.runGeneration();

		assertExists("build/generated/source/typescript/index.ts");
		assertExists("build/generated/source/typescript/project.ts");
		assertExists("build/generated/source/typescript/project.data.ts");
		assertExists("build/generated/source/typescript/schedule.ts");
		assertExists("build/generated/source/typescript/task.ts");
		assertNotExists("build/generated/source/typescript/task.links.ts");
		assertNotExists("build/generated/source/typescript/task.meta.ts");

		Charset utf8 = Charset.forName("UTF8");
		String expectedSourceFileName = expressions ? "expected_schedule_with_expressions.ts" :
				"expected_schedule_without_expressions.ts";
		String expectedSource = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(expectedSourceFileName), utf8);
		String actualSource = IOUtils
				.toString(new FileInputStream(new File(outputDir, "build/generated/source/typescript/schedule.ts")), utf8);
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
