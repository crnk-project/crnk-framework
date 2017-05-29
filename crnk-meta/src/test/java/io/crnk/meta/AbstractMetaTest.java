package io.crnk.meta;

import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.url.ConstantServiceUrlProvider;
import io.crnk.core.module.discovery.ReflectionsServiceDiscovery;
import io.crnk.legacy.locator.SampleJsonServiceLocator;
import io.crnk.rs.internal.JaxrsModule;
import org.junit.Before;

public abstract class AbstractMetaTest {

	protected CrnkBoot boot;

	@Before
	public void setup() {
		boot = new CrnkBoot();
		boot.addModule(new JaxrsModule(null));
		boot.setServiceUrlProvider(new ConstantServiceUrlProvider("http://localhost"));
		boot.setServiceDiscovery(new ReflectionsServiceDiscovery("io.crnk.test.mock.repository", new SampleJsonServiceLocator
				()));
		configure();
		boot.boot();
	}

	protected void configure() {

	}
}
