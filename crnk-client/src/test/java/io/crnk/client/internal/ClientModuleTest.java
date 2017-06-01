package io.crnk.client.internal;

import io.crnk.client.module.ClientModule;
import org.junit.Assert;
import org.junit.Test;

public class ClientModuleTest {

	@Test
	public void testName() {
		ClientModule module = new ClientModule();
		Assert.assertEquals("client", module.getModuleName());
	}
}
