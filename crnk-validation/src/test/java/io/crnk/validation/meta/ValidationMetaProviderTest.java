package io.crnk.validation.meta;

import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.url.ConstantServiceUrlProvider;
import io.crnk.core.module.discovery.ReflectionsServiceDiscovery;
import io.crnk.legacy.locator.SampleJsonServiceLocator;
import io.crnk.meta.MetaLookup;
import io.crnk.meta.model.MetaAttribute;
import io.crnk.meta.model.resource.MetaResourceBase;
import io.crnk.meta.provider.resource.ResourceMetaProvider;
import io.crnk.rs.internal.JaxrsModule;
import io.crnk.validation.mock.models.Task;
import org.junit.Assert;
import org.junit.Test;

public class ValidationMetaProviderTest {

	private MetaLookup lookup;

	private ResourceMetaProvider resourceMetaProvider;

	private void setup(boolean addValidationProvider) {
		CrnkBoot boot = new CrnkBoot();
		boot.addModule(new JaxrsModule(null));
		boot.setServiceUrlProvider(new ConstantServiceUrlProvider("http://localhost"));
		boot.setServiceDiscovery(
				new ReflectionsServiceDiscovery("io.crnk.validation.mock", new SampleJsonServiceLocator()));
		boot.boot();

		resourceMetaProvider = new ResourceMetaProvider();
		lookup = new MetaLookup();
		lookup.setModuleContext(boot.getModuleRegistry().getContext());
		lookup.addProvider(resourceMetaProvider);
		if (addValidationProvider) {
			lookup.addProvider(new ValidationMetaProvider());
		}
		lookup.initialize();
	}

	@Test
	public void testNotNullNotDisabledWithoutValidationProvider() {
		setup(false);
		MetaResourceBase meta = resourceMetaProvider.getMeta(Task.class);
		MetaAttribute attr = meta.getAttribute("name");
		Assert.assertTrue(attr.isNullable());
	}

	@Test
	public void testNotNullDisablesNullablity() {
		setup(true);
		MetaResourceBase meta = resourceMetaProvider.getMeta(Task.class);
		MetaAttribute attr = meta.getAttribute("name");
		Assert.assertFalse(attr.isNullable());
	}
}
