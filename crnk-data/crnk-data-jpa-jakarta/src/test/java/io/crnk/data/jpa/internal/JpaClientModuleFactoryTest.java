package io.crnk.data.jpa.internal;

import io.crnk.client.module.ClientModuleFactory;
import io.crnk.core.module.Module;
import io.crnk.data.jpa.JpaModule;
import io.crnk.meta.MetaModule;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashSet;
import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.Set;

public class JpaClientModuleFactoryTest {

	@Test
	public void test() {
		ServiceLoader<ClientModuleFactory> loader = ServiceLoader.load(ClientModuleFactory.class);
		Iterator<ClientModuleFactory> iterator = loader.iterator();

		Set<Class> moduleClasses = new HashSet<>();
		while (iterator.hasNext()) {
			ClientModuleFactory moduleFactory = iterator.next();
			Module module = moduleFactory.create();
			moduleClasses.add(module.getClass());
		}

		Assert.assertEquals(2, moduleClasses.size());
		Assert.assertTrue(moduleClasses.contains(JpaModule.class));
		Assert.assertTrue(moduleClasses.contains(MetaModule.class));
	}
}
