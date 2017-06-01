package io.crnk.core.module.discovery;

import org.junit.Assert;
import org.junit.Test;

public class DefaultServiceDiscoveryFactoryTest {

	@Test
	public void test() {
		DefaultServiceDiscoveryFactory factory = new DefaultServiceDiscoveryFactory();
		ServiceDiscovery instance = factory.getInstance();
		Assert.assertTrue(instance instanceof TestServiceDiscovery);
	}
}
