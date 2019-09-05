package io.crnk.gen.openapi;

import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.module.discovery.EmptyServiceDiscovery;
import io.crnk.meta.MetaModule;
import io.crnk.meta.MetaModuleConfig;
import io.crnk.meta.provider.resource.ResourceMetaProvider;
import io.crnk.test.mock.SimpleTestModule;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

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

	@Test
	public void testGeneration() throws IOException {
		File buildDir = new File("build/tmp/openapi");
		generatorModule = new OpenAPIGeneratorModule();
		generatorModule.getConfig().setBuildDir(buildDir);
		generatorModule.getConfig().setTemplateName("openapi-template.yml");
		generatorModule.initDefaults(buildDir);
		generatorModule.generate(metaModule.getLookup());
	}
}
