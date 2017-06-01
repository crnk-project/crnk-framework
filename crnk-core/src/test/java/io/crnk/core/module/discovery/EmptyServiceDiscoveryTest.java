package io.crnk.core.module.discovery;

import org.junit.Assert;
import org.junit.Test;

public class EmptyServiceDiscoveryTest {

	@Test
	public void test() {
		EmptyServiceDiscovery discovery = new EmptyServiceDiscovery();
		Assert.assertEquals(0, discovery.getInstancesByType(null).size());
		Assert.assertEquals(0, discovery.getInstancesByAnnotation(null).size());
	}
}
