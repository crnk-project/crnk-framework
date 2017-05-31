package io.crnk.core.module.discovery;

import io.crnk.core.boot.CrnkProperties;
import io.crnk.core.engine.properties.NullPropertiesProvider;
import io.crnk.core.engine.properties.PropertiesProvider;
import io.crnk.legacy.locator.SampleJsonServiceLocator;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class FallbackServiceDiscoveryFactoryTest {

	@Test
	public void returnDefaultDiscoveryIfAvailable() {
		PropertiesProvider propertiesProvider = new NullPropertiesProvider();
		DefaultServiceDiscoveryFactory defaultFactory = new DefaultServiceDiscoveryFactory();
		FallbackServiceDiscoveryFactory fallbackFactory = new FallbackServiceDiscoveryFactory(defaultFactory,
				new SampleJsonServiceLocator(), propertiesProvider);
		Assert.assertTrue(fallbackFactory.getInstance() instanceof TestServiceDiscovery);

	}

	@Test
	public void fallbackToReflectionsIfNoDefaultAvailable() {
		PropertiesProvider propertiesProvider = Mockito.mock(PropertiesProvider.class);
		Mockito.when(propertiesProvider.getProperty(Mockito.eq(CrnkProperties.RESOURCE_SEARCH_PACKAGE))).thenReturn("a.b.c");
		ServiceDiscoveryFactory defaultFactory = new ServiceDiscoveryFactory() {
			@Override
			public ServiceDiscovery getInstance() {
				return null;
			}
		};
		FallbackServiceDiscoveryFactory fallbackFactory = new FallbackServiceDiscoveryFactory(defaultFactory,
				new SampleJsonServiceLocator(), propertiesProvider);
		Assert.assertTrue(fallbackFactory.getInstance() instanceof ReflectionsServiceDiscovery);

	}

	@Test
	public void fallbackToEmptyDiscoveryIfNoPackageAndDefaultAvailable() {
		PropertiesProvider propertiesProvider = Mockito.mock(PropertiesProvider.class);
		ServiceDiscoveryFactory defaultFactory = new ServiceDiscoveryFactory() {
			@Override
			public ServiceDiscovery getInstance() {
				return null;
			}
		};
		FallbackServiceDiscoveryFactory fallbackFactory = new FallbackServiceDiscoveryFactory(defaultFactory,
				new SampleJsonServiceLocator(), propertiesProvider);
		Assert.assertTrue(fallbackFactory.getInstance() instanceof EmptyServiceDiscovery);
	}
}
