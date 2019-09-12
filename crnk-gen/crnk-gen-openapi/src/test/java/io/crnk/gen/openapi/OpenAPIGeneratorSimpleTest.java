package io.crnk.gen.openapi;

import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.module.discovery.EmptyServiceDiscovery;
import io.crnk.meta.MetaModule;
import io.crnk.meta.MetaModuleConfig;
import io.crnk.meta.provider.resource.ResourceMetaProvider;
import io.crnk.test.mock.SimpleTestModule;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.parser.OpenAPIV3Parser;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class OpenAPIGeneratorSimpleTest {

	private CrnkBoot crnkBoot;
	private MetaModule metaModule;
	private OpenAPIGeneratorModule generatorModule;

	@Before
	public void setup() throws IOException {
		File buildDir = new File("build/tmp/openapi");

		InputStream xmlDoc = getClass().getClassLoader().getResourceAsStream("openapi-template.yml");
		FileUtils.copyInputStreamToFile(xmlDoc, new File(buildDir, "openapi-template.yml"));

		MetaModuleConfig metaConfig = new MetaModuleConfig();
		metaConfig.addMetaProvider(new ResourceMetaProvider());
		metaModule = MetaModule.createServerModule(metaConfig);

		crnkBoot = new CrnkBoot();
		crnkBoot.setServiceDiscovery(new EmptyServiceDiscovery());
		crnkBoot.addModule(new SimpleTestModule());
		crnkBoot.addModule(metaModule);
		crnkBoot.boot();
	}

	private static void assertOperationResponseCodes(Operation operation, List<String> codes) {
		if (operation == null) {
			return;
		}
		ApiResponses responses = operation.getResponses();
		for (String code : codes) {
			Assert.assertTrue("Operation missing response code " + code, responses.containsKey(code));
		}
	}

	@Test
	public void testGeneration() throws IOException {
		File buildDir = new File("build/tmp/openapi");
		String outputPath = buildDir.toString() + "/generated/source/openapi/openapi.yaml";
		String templatePath = buildDir.toString() + "/openapi-template.yml";
		generatorModule = new OpenAPIGeneratorModule();
		generatorModule.getConfig().setBuildDir(buildDir);
		generatorModule.getConfig().setTemplateName("openapi-template.yml");
		generatorModule.initDefaults(buildDir);
		generatorModule.generate(metaModule.getLookup());
		OpenAPI openApi = new OpenAPIV3Parser().read(outputPath);
		OpenAPI openApiTemplate = new OpenAPIV3Parser().read(templatePath);
		Yaml.pretty(openApi);

		// Compare templated metadata and generated metadata
		Assert.assertEquals(openApiTemplate.getOpenapi(), openApi.getOpenapi());
		Assert.assertEquals(openApiTemplate.getInfo(), openApi.getInfo());
		Assert.assertEquals(openApiTemplate.getServers(), openApi.getServers());

		Operation templateGetTasks = openApiTemplate.getPaths().get("/tasks").getGet();
		Operation generatedGetTasks = openApi.getPaths().get("/tasks").getGet();

		// Compare templated and generated GET /tasks
		Assert.assertEquals(templateGetTasks.getSummary(), generatedGetTasks.getSummary());
		Assert.assertEquals(templateGetTasks.getDescription(), generatedGetTasks.getDescription());
		Assert.assertEquals(templateGetTasks.getOperationId(), generatedGetTasks.getOperationId());
		Assert.assertEquals(templateGetTasks.getExtensions(), generatedGetTasks.getExtensions());

		// Compare templated and generated GET /tasks
		Operation templateGetTaskById = openApiTemplate.getPaths().get("/tasks/{id}").getGet();
		Operation generatedGetTaskById = openApi.getPaths().get("/tasks/{id}").getGet();
		Assert.assertEquals(templateGetTaskById.getExtensions(), generatedGetTaskById.getExtensions());

		// Ensure responses satisfy minimal requirements for JSON:API compliance
		for (PathItem pathItem: openApi.getPaths().values()) {

			// Ensure GET response json:api compliance
			assertOperationResponseCodes(pathItem.getGet(), Arrays.asList("200", "400"));

			// Ensure POST response json:api compliance
			assertOperationResponseCodes(pathItem.getPost(), Arrays.asList("201", "202", "204", "403", "404", "409"));

			// Ensure PATCH response json:api compliance
			assertOperationResponseCodes(pathItem.getPatch(), Arrays.asList("200", "202", "204", "403", "404", "409"));

			// Ensure DELETE response json:api compliance
			assertOperationResponseCodes(pathItem.getDelete(), Arrays.asList("200", "202", "204", "404"));

			for (Operation operation: pathItem.readOperationsMap().values()) {

				// TODO: Ensure responses are sorted
				 List<String> responses = new ArrayList<>(operation.getResponses().keySet());
				 List<String> sorted = new ArrayList<>(responses);
				 Collections.sort(sorted);
				 Assert.assertEquals("Responses should be sorted.", sorted, responses);
				
			}
		}
	}
}
