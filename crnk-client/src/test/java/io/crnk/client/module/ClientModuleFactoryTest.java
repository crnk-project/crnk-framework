package io.crnk.client.module;

import java.util.List;

import io.crnk.client.CrnkClient;
import io.crnk.core.engine.internal.jackson.JacksonModule;
import io.crnk.core.module.Module;
import org.junit.Assert;
import org.junit.Test;

public class ClientModuleFactoryTest {

	@Test
	public void shouldDiscoverModules() {
		CrnkClient client = new CrnkClient("http://something");
		client.findModules();
		List<Module> modules = client.getModuleRegistry().getModules();

		Assert.assertEquals(3, modules.size());
		Assert.assertEquals(ClientModule.class, modules.get(0).getClass());
		Assert.assertEquals(JacksonModule.class, modules.get(1).getClass());
		Assert.assertEquals(ClientTestModule.class, modules.get(2).getClass());
	}

	@Test
	public void shouldDiscoverObjectLinkModules() {
		CrnkClient client = new CrnkClient("http://something", CrnkClient.ClientType.OBJECT_LINKS);
		client.findModules();
		List<Module> modules = client.getModuleRegistry().getModules();

		Assert.assertEquals(3, modules.size());
		Assert.assertEquals(ClientModule.class, modules.get(0).getClass());
		Assert.assertEquals(JacksonModule.class, modules.get(1).getClass());
		Assert.assertEquals(ClientTestModule.class, modules.get(2).getClass());
	}
}
