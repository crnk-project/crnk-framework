package io.crnk.client.ngrx.gen;


import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.module.discovery.EmptyServiceDiscovery;
import io.crnk.gen.typescript.TSGeneratorConfig;
import io.crnk.gen.typescript.internal.TSGenerator;
import io.crnk.meta.MetaModule;
import io.crnk.meta.model.MetaElement;
import io.crnk.meta.provider.resource.ResourceMetaProvider;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;

public class GeneratorExecutor {

	public static void main(String[] args) {
		GeneratorExecutor executor = new GeneratorExecutor();

		File outputDir = new File("crnk-client-angular-ngrx");
		executor.run(outputDir);

	}

	public void run(File outputDir) {
		TSGeneratorConfig config = new TSGeneratorConfig();
		config.setGenerateExpressions(true);
		config.getNpm().setPackagingEnabled(false);
		config.getNpm().setPackageName("@crnk/angular-ngrx");
		config.getNpm().getPackageMapping().put(MetaElement.class.getPackage().getName(), "@crnk/angular-ngrx/meta");
		config.setExcludes(new HashSet<>());

		MetaModule metaModule = MetaModule.create();
		metaModule.addMetaProvider(new ResourceMetaProvider());

		CrnkBoot boot = new CrnkBoot();
		boot.setServiceDiscovery(new EmptyServiceDiscovery());
		boot.addModule(metaModule);
		boot.boot();

		TSGenerator generator = new TSGenerator(outputDir, metaModule.getLookup(), config);
		try {
			generator.run();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}
}
