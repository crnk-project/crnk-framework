package io.crnk.meta;

import java.util.List;

import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.RegistryEntryBuilder;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.engine.url.ConstantServiceUrlProvider;
import io.crnk.core.module.ModuleRegistry;
import io.crnk.meta.model.resource.MetaResource;
import io.crnk.meta.provider.resource.ResourceMetaProvider;
import io.crnk.rs.internal.JaxrsModule;
import io.crnk.test.mock.models.Task;
import io.crnk.test.mock.repository.TaskRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class MetaModuleTest {

	private MetaModuleConfig metaModuleConfig;

	private MetaModule metaModule;

	private CrnkBoot boot;

	@Before
	public void setup() {
		metaModuleConfig = new MetaModuleConfig();
		metaModuleConfig.addMetaProvider(new ResourceMetaProvider());
		metaModule = MetaModule.createServerModule(metaModuleConfig);

		boot = new CrnkBoot();
		boot.addModule(new JaxrsModule(null));
		boot.setServiceUrlProvider(new ConstantServiceUrlProvider("http://localhost"));
		boot.addModule(metaModule);
		boot.boot();
	}


	@Test
	public void checkName() {
		MetaModule module = MetaModule.createClientModule();
		Assert.assertEquals("meta", module.getModuleName());
	}

	@Test
	public void checkHasProtectedConstructor() {
		// TODO ClassTestUtils.assertProtectedConstructor(MetaModuleTest.class);
	}

	@Test
	public void checkRequestLocalCaching() {
		// get instance, triggers store in request local
		MetaLookup lookup = metaModule.getLookup();

		ThreadLocal<MetaLookup> lookupRequestLocal = metaModule.getLookupRequestLocal();
		Assert.assertNotNull(lookupRequestLocal.get());

		// clear global instance, still in request local
		metaModule.reset();
		Assert.assertSame(lookup, metaModule.getLookup());

		// clear request local, new instance to be returned
		metaModule.getLookupRequestLocal().remove();
		Assert.assertNotSame(lookup, metaModule.getLookup());
	}


	@Test
	public void checkRegistryUpdateTriggerMetaUpdate() {
		MetaLookup lookup = metaModule.getLookup();
		List<MetaResource> prevResources = lookup.findElements(MetaResource.class);

		ResourceRegistry resourceRegistry = boot.getResourceRegistry();
		resourceRegistry.addEntry(createRegistryEntry());

		// check request local caching against concurrent modifications
		Assert.assertEquals(prevResources.size(), metaModule.getLookup().findElements(MetaResource.class).size());

		// check new meta available for next request
		metaModule.getLookupRequestLocal().remove();
		Assert.assertEquals(prevResources.size() + 1, metaModule.getLookup().findElements(MetaResource.class).size());
	}

	private RegistryEntry createRegistryEntry() {
		ModuleRegistry moduleRegistry = boot.getModuleRegistry();
		RegistryEntryBuilder entryBuilder = moduleRegistry.getContext().newRegistryEntryBuilder();
		entryBuilder.resource().resourceClass(Task.class).resourceType("someNewTask");
		entryBuilder.resourceRepository().instance(new TaskRepository());
		return entryBuilder.build();
	}

}
