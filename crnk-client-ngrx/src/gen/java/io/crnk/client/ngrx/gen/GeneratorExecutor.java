package io.crnk.client.ngrx.gen;


import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.module.discovery.EmptyServiceDiscovery;
import io.crnk.gen.typescript.TSGeneratorConfiguration;
import io.crnk.gen.typescript.internal.TSGenerator;
import io.crnk.meta.MetaModule;
import io.crnk.meta.model.MetaElement;
import io.crnk.meta.provider.resource.ResourceMetaProvider;

import java.io.File;
import java.io.IOException;

public class GeneratorExecutor {

	public static void main(String[] args) {
		new GeneratorExecutor().run();
	}

	public void run() {
		TSGeneratorConfiguration config = new TSGeneratorConfiguration();
		config.setSourceDirectoryName("meta");
		config.setGenerateExpressions(true);
		config.getNpm().setEnabled(false);
		config.getNpm().setPackageName("@crnk/ngrx");
		config.getNpm().getPackageMapping().put(MetaElement.class.getPackage().getName(), "@crnk/ngrx");

		File outputDir = new File("src/typescript");

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
