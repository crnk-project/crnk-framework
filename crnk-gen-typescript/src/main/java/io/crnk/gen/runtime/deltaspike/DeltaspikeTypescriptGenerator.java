package io.crnk.gen.runtime.deltaspike;

import java.io.File;
import javax.inject.Singleton;

import io.crnk.gen.typescript.TSGenerator;
import io.crnk.gen.typescript.TSGeneratorConfiguration;
import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.url.ConstantServiceUrlProvider;
import io.crnk.core.module.ModuleRegistry;
import io.crnk.core.utils.Optional;
import io.crnk.meta.MetaLookup;
import io.crnk.meta.MetaModule;
import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(CdiTestRunner.class)
@Singleton
public class DeltaspikeTypescriptGenerator {

	private static File outputDir;

	private static TSGeneratorConfiguration config;

	@Test
	public void test() {
		CrnkBoot boot = new CrnkBoot();
		boot.setServiceUrlProvider(new ConstantServiceUrlProvider("http://<generator>"));
		boot.boot();

		ModuleRegistry moduleRegistry = boot.getModuleRegistry();
		Optional<MetaModule> optionalModule = moduleRegistry.getModule(MetaModule.class);

		if (!optionalModule.isPresent()) {
			throw new IllegalStateException("add MetaModule to CDI setup, got: " + moduleRegistry.getModules());
		}

		MetaModule metaModule = optionalModule.get();
		MetaLookup lookup = metaModule.getLookup();

		TSGenerator gen = new TSGenerator(outputDir, lookup, config);
		gen.run();
	}

	public static void setConfig(TSGeneratorConfiguration config) {
		DeltaspikeTypescriptGenerator.config = config;
	}

	public static void setOutputDir(File outputDir) {
		DeltaspikeTypescriptGenerator.outputDir = outputDir;
	}
}
