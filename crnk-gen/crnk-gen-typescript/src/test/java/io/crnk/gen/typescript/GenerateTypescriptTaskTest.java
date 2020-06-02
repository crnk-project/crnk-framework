package io.crnk.gen.typescript;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.module.Module;
import io.crnk.core.module.SimpleModule;
import io.crnk.core.repository.InMemoryResourceRepository;
import io.crnk.data.facet.FacetModule;
import io.crnk.data.facet.FacetModuleConfig;
import io.crnk.gen.typescript.model.DottedResourceName;
import io.crnk.meta.MetaLookup;
import io.crnk.meta.MetaModule;
import io.crnk.meta.MetaModuleConfig;
import io.crnk.meta.provider.resource.ResourceMetaProvider;
import io.crnk.test.mock.repository.PrimitiveAttributeRepository;
import io.crnk.test.mock.repository.ProjectRepository;
import io.crnk.test.mock.repository.ProjectToTaskRepository;
import io.crnk.test.mock.repository.ScheduleRepositoryImpl;
import io.crnk.test.mock.repository.ScheduleStatusRepositoryImpl;
import io.crnk.test.mock.repository.TaskRepository;
import io.crnk.test.mock.repository.TaskSubtypeRepository;
import io.crnk.test.mock.repository.TaskToProjectRepository;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GenerateTypescriptTaskTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(GenerateTypescriptTaskTest.class);


	private File outputDir;

	@Test
	public void testNgrxWithExpressions() throws IOException {
		test(true, TSResourceFormat.NGRX);
	}

	@Test
	public void testCrnkWithExpressions() throws IOException {
		test(true, TSResourceFormat.NGRX_CRNK);
	}

	@Test
	public void testCrnkWithoutExpressions() throws IOException {
		test(false, TSResourceFormat.NGRX_CRNK);
	}

	@Test
	public void testPlainJson() throws IOException {
		test(false, TSResourceFormat.PLAINJSON);
	}

	private void test(boolean expressions, TSResourceFormat resourceFormat) throws IOException {
		outputDir = new File("build/tmp/gen");
		FileUtils.deleteDirectory(outputDir);
		outputDir.mkdirs();

		File npmrcFile = new File(outputDir, ".npmrc");
		FileWriter npmrcWriter = new FileWriter(npmrcFile);
		npmrcWriter.write("");
		npmrcWriter.close();


		MetaLookup lookup = createLookup();

		TSGeneratorModule module = new TSGeneratorModule();
		createConfig(module.getConfig(), resourceFormat, expressions);
		module.getConfig().setGenDir(outputDir);
		module.initDefaults(outputDir);
		module.generate(lookup);

		assertExists("index.ts");
		assertExists("index.ts");
		assertExists("projects.ts");
		assertExists("primitive.attribute.ts");
		assertExists("types/project.data.ts");
		assertExists("schedule.ts");
		assertExists("tasks.ts");
		assertExists("facet.ts");
		assertExists("facet.value.ts");
		assertExists("some/dotted.resource.ts");
		assertNotExists("tasks.links.ts");
		assertNotExists("tasks.meta.ts");

		if (resourceFormat == TSResourceFormat.PLAINJSON) {
			assertExists("crnk.ts");
			checkPrimitiveAttributes();
		} else if (resourceFormat == TSResourceFormat.NGRX) {
			assertExists("crnk.ts");
		}

		checkProject();
		checkSchedule(expressions, resourceFormat);
		if (expressions) {
			checkProjectData(resourceFormat);
		}
	}

	private void checkPrimitiveAttributes() throws IOException {
		String expectedSourceFileName = "expected_primitive_plain_json.ts";
		String actualSourcePath = "primitive.attribute.ts";
		compare(expectedSourceFileName, actualSourcePath);
	}

	private TSGeneratorConfig createConfig(TSGeneratorConfig tsConfig, TSResourceFormat resourceFormat, boolean expressions) {
		String testPackage = "@crnk/gen-typescript-test";
		tsConfig.getNpm().setPackageName(testPackage);
		tsConfig.getNpm().setGitRepository("someThing");
		tsConfig.getNpm().getPackageMapping().put("io.crnk.test.mock.models", testPackage);
		tsConfig.getNpm().getPackageMapping().put("io.crnk.meta", testPackage);
		tsConfig.getNpm().setPackageVersion("0.0.1");
		tsConfig.setExpressions(expressions);
		tsConfig.setFormat(resourceFormat);
		return tsConfig;
	}

	private MetaLookup createLookup() {
		MetaModule metaModule = createMetaModule();
		CrnkBoot boot = new CrnkBoot();
		boot.addModule(metaModule);
		boot.addModule(createRepositoryModule());
		boot.addModule(new FacetModule(new FacetModuleConfig()));
		boot.boot();
		return metaModule.getLookup();
	}

	public MetaModule createMetaModule() {
		MetaModuleConfig metaConfig = new MetaModuleConfig();
		metaConfig.addMetaProvider(new ResourceMetaProvider());
		MetaModule metaModule = MetaModule.createServerModule(metaConfig);
		return metaModule;
	}

	public Module createRepositoryModule() {
		SimpleModule module = new SimpleModule("mock");
		module.addRepository(new ScheduleRepositoryImpl());
		module.addRepository(new ProjectRepository());
		module.addRepository(new TaskRepository());
		module.addRepository(new ProjectToTaskRepository());
		module.addRepository(new TaskSubtypeRepository());
		module.addRepository(new TaskToProjectRepository());
		module.addRepository(new ScheduleStatusRepositoryImpl());
		module.addRepository(new PrimitiveAttributeRepository());
		module.addRepository(new InMemoryResourceRepository(DottedResourceName.class));
		return module;
	}

	private void checkProjectData(TSResourceFormat format) throws IOException {
		if (format == TSResourceFormat.NGRX_CRNK) {
			String expectedSourceFileName = "expected_project_data.ts";
			String actualSourcePath = "types/project.data.ts";
			compare(expectedSourceFileName, actualSourcePath);
		}
	}

	private void checkProject() throws IOException {
		Charset utf8 = Charset.forName("UTF8");
		try (InputStream in = new FileInputStream(new File(outputDir, "projects.ts"))) {
			String actualSource = IOUtils
					.toString(in, utf8);
			Assert.assertTrue(actualSource.contains(" from './types/project.data'"));
		}
	}

	private void checkSchedule(boolean expressions, TSResourceFormat format) throws IOException {
		String expectedSourceFileName;
		if (format == TSResourceFormat.PLAINJSON) {
			expectedSourceFileName = "expected_schedule_plain_json.ts";
		} else if (format == TSResourceFormat.NGRX) {
			expectedSourceFileName = "expected_schedule_ngrx.ts";
		} else if (expressions) {
			expectedSourceFileName = "expected_schedule_with_expressions.ts";
		} else {
			expectedSourceFileName = "expected_schedule_without_expressions.ts";
		}

		String actualSourcePath = "schedule.ts";
		compare(expectedSourceFileName, actualSourcePath);
	}

	private void compare(String expectedSourceFileName, String actualSourcePath) throws IOException {
		Charset utf8 = Charset.forName("UTF8");

		String expectedSource;
		try (InputStream in = getClass().getClassLoader().getResourceAsStream(expectedSourceFileName)) {
			expectedSource = IOUtils.toString(in, utf8);
		}

		String actualSource;
		try (FileInputStream in = new FileInputStream(new File(outputDir, actualSourcePath))) {
			actualSource = IOUtils.toString(in, utf8);
		}

		expectedSource = expectedSource.replace("\r\n", "\n");

		LoggerFactory.getLogger(getClass()).info(actualSource);
		LOGGER.info(actualSource);
		System.err.println(actualSource);

		String[] expectedLines = org.apache.commons.lang3.StringUtils.split(expectedSource, '\n');
		String[] actualLines = org.apache.commons.lang3.StringUtils.split(actualSource, '\n');
		for (int i = 0; i < expectedLines.length; i++) {
			Assert.assertEquals("line: " + i + ", " + expectedLines[i], expectedLines[i], actualLines[i]);
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
