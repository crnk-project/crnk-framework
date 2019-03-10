package io.crnk.gen.runtime.cdi;

import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.url.ConstantServiceUrlProvider;
import io.crnk.core.module.ModuleRegistry;
import io.crnk.gen.runtime.GeneratorTrigger;
import io.crnk.meta.MetaLookup;
import io.crnk.meta.MetaModule;
import org.jboss.weld.environment.se.Weld;

import java.io.IOException;
import java.util.Optional;

public class CdiRunner {

	public void run(GeneratorTrigger context) throws IOException {
		Weld weld = new Weld();
		try {
			weld.setClassLoader(context.getClassLoader());
			weld.initialize();

			CrnkBoot boot = new CrnkBoot();
			boot.setServiceUrlProvider(new ConstantServiceUrlProvider("http://<generator>"));
			boot.boot();

			ModuleRegistry moduleRegistry = boot.getModuleRegistry();
			Optional<MetaModule> optionalModule = moduleRegistry.getModule(MetaModule.class);

			if (!optionalModule.isPresent()) {
				throw new IllegalStateException(
						"add MetaModule to CDI setup, got: " + moduleRegistry.getModules() + " with " + boot
								.getServiceDiscovery());
			}

			MetaModule metaModule = optionalModule.get();
			MetaLookup lookup = metaModule.getLookup();
			context.generate(lookup);
		} finally {
			weld.shutdown();
		}

	}
}
