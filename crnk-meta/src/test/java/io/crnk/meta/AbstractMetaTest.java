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

	protected ResourceMetaProvider resourceProvider;

	@Before
	public void setup() {
		boot = new CrnkBoot();
		boot.addModule(new JaxrsModule(null));
		boot.setServiceUrlProvider(new ConstantServiceUrlProvider("http://localhost"));
		boot.addModule(new TestModule());
		configure();
		boot.boot();

		resourceProvider = new ResourceMetaProvider();

		lookup = new MetaLookup();
		lookup.addProvider(resourceProvider);
		lookup.setModuleContext(boot.getModuleRegistry().getContext());
		lookup.initialize();
	}

	protected void configure() {

	}
}
