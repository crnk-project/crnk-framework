package io.crnk.client.module;

import io.crnk.client.CrnkClient;
import io.crnk.core.engine.internal.jackson.JacksonModule;
import io.crnk.core.module.Module;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class ClientModuleFactoryTest {

	@Test
	public void shouldDiscoverModules() {
		CrnkClient client = new CrnkClient("http://something");
		client.findModules();
		List<Module> modules = client.getModuleRegistry().getModules();

		Assert.assertEquals(3, modules.size());
		Assert.assertEquals(ClientModule.class, modules.get(0).getClass());
		Assert.assertEquals(JacksonModule.class, modules.get(1).getClass());
		Assert.assertEquals(TestModule.class, modules.get(2).getClass());
	}
}
