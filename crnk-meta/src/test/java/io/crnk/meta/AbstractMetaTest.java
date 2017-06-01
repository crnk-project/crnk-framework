package io.crnk.meta;

import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.url.ConstantServiceUrlProvider;
import io.crnk.meta.provider.resource.ResourceMetaProvider;
import io.crnk.rs.internal.JaxrsModule;
import io.crnk.test.mock.TestModule;
import org.junit.Before;

public abstract class AbstractMetaTest {

	protected CrnkBoot boot;

	protected MetaLookup lookup;

	@Before
	public void setup() {
		boot = new CrnkBoot();
		boot.addModule(new JaxrsModule(null));
		boot.setServiceUrlProvider(new ConstantServiceUrlProvider("http://localhost"));
		boot.addModule(new TestModule());
		configure();
		boot.boot();

		lookup = new MetaLookup();
		lookup.addProvider(new ResourceMetaProvider());
		lookup.setModuleContext(boot.getModuleRegistry().getContext());
		lookup.initialize();
	}

	protected void configure() {

	}
}
