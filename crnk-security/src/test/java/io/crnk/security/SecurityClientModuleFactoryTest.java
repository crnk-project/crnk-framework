package io.crnk.security;

import io.crnk.client.module.ClientModuleFactory;
import io.crnk.core.module.Module;
import org.junit.Assert;
import org.junit.Test;

import java.util.Iterator;
import java.util.ServiceLoader;

public class SecurityClientModuleFactoryTest {

	@Test
	public void test() {
		ServiceLoader<ClientModuleFactory> loader = ServiceLoader.load(ClientModuleFactory.class);
		Iterator<ClientModuleFactory> iterator = loader.iterator();
		Assert.assertTrue(iterator.hasNext());
		ClientModuleFactory moduleFactory = iterator.next();
		Assert.assertFalse(iterator.hasNext());
		Module module = moduleFactory.create();
		Assert.assertTrue(module instanceof SecurityModule);
	}
}
