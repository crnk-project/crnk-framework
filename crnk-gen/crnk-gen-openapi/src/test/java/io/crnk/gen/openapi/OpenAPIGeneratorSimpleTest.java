package io.crnk.gen.openapi;

import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.module.discovery.EmptyServiceDiscovery;
import io.crnk.meta.MetaModule;
import io.crnk.meta.MetaModuleConfig;
import io.crnk.meta.provider.resource.ResourceMetaProvider;
import io.crnk.test.mock.SimpleTestModule;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class OpenAPIGeneratorSimpleTest {

	private CrnkBoot crnkBoot;
	private MetaModule metaModule;
	private OpenAPIGeneratorModule generatorModule;

	@Before
	public void setup() {
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
		generatorModule.initDefaults(buildDir);
		generatorModule.generate(metaModule.getLookup());
	}
}
