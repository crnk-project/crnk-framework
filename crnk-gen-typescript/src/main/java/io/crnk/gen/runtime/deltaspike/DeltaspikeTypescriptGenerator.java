package io.crnk.gen.runtime.deltaspike;

import javax.inject.Singleton;

import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.url.ConstantServiceUrlProvider;
import io.crnk.core.module.ModuleRegistry;
import io.crnk.core.utils.Optional;
import io.crnk.gen.runtime.GeneratorTrigger;
import io.crnk.meta.MetaLookup;
import io.crnk.meta.MetaModule;
import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

/**
 * Resolution of MetaLookup implemented as test case. There are nicer things to look at, but it is simple and serves its
 * purpose.
 */
@RunWith(CdiTestRunner.class)
@Singleton
public class DeltaspikeTypescriptGenerator {

	private static GeneratorTrigger context;

	@Test
	public void test() throws IOException {
		CrnkBoot boot = new CrnkBoot();
		boot.setServiceUrlProvider(new ConstantServiceUrlProvider("http://<generator>"));
		boot.boot();

		ModuleRegistry moduleRegistry = boot.getModuleRegistry();
		Optional<MetaModule> optionalModule = moduleRegistry.getModule(MetaModule.class);

		if (!optionalModule.isPresent()) {
			throw new IllegalStateException("add MetaModule to CDI setup, got: " + moduleRegistry.getModules() + " with " + boot.getServiceDiscovery());
		}

		MetaModule metaModule = optionalModule.get();
		MetaLookup lookup = metaModule.getLookup();

		context.generate(lookup);
	}

	public static void setContext(GeneratorTrigger context) {
		DeltaspikeTypescriptGenerator.context = context;
	}
}
