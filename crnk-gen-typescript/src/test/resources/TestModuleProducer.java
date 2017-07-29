package io.crnk.gen.typescript;

import io.crnk.meta.MetaModule;
import io.crnk.meta.provider.resource.ResourceMetaProvider;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TestModuleProducer {
	@ApplicationScoped
	public MetaModule createMetaModule() {
		MetaModule metaModule = MetaModule.create();
		metaModule.addMetaProvider(new ResourceMetaProvider());
		return metaModule;
	}
}
