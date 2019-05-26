package io.crnk.meta;

import io.crnk.meta.provider.resource.ResourceMetaProvider;
import io.crnk.rs.internal.JaxrsModule;
import io.crnk.test.TestContainer;
import org.junit.Before;

public abstract class AbstractMetaTest {


	protected MetaLookupImpl lookup;

	protected ResourceMetaProvider resourceProvider;

	protected TestContainer container;

	@Before
	public void setup() {
		container = new TestContainer();
		container.getBoot().getCoreModule()
				.setDefaultRepositoryInformationProvider(new JaxrsModule.JaxrsResourceRepositoryInformationProvider());
		container.addModule(new JaxrsModule(null));
		container.addTestModule();
		container.addModule(new io.crnk.test.mock.dynamic.DynamicModule());
		configure();
		container.boot();

		resourceProvider = new ResourceMetaProvider();

		lookup = new MetaLookupImpl();
		lookup.addProvider(resourceProvider);
		lookup.setModuleContext(container.getModuleRegistry().getContext());
		lookup.initialize();
	}

	protected void configure() {

	}
}
