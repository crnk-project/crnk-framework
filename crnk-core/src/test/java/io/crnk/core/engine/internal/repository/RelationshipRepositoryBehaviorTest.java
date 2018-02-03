package io.crnk.core.engine.internal.repository;

import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.engine.url.ConstantServiceUrlProvider;
import io.crnk.core.exception.RelationshipRepositoryNotFoundException;
import io.crnk.core.mock.MockConstants;
import io.crnk.core.mock.models.RelationshipBehaviorTestResource;
import io.crnk.core.module.discovery.ReflectionsServiceDiscovery;
import io.crnk.core.repository.RelationshipRepositoryBase;
import io.crnk.core.repository.implicit.ImplicitOwnerBasedRelationshipRepository;
import io.crnk.core.resource.registry.ResourceRegistryTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class RelationshipRepositoryBehaviorTest {

	private ResourceRegistry resourceRegistry;

	@Before
	public void setup() {
		CrnkBoot boot = new CrnkBoot();
		boot.setServiceUrlProvider(new ConstantServiceUrlProvider(ResourceRegistryTest.TEST_MODELS_URL));
		boot.setServiceDiscovery(new ReflectionsServiceDiscovery(MockConstants.TEST_MODELS_PACKAGE));
		boot.boot();
		resourceRegistry = boot.getResourceRegistry();
	}

	@Test
	public void checkRelationIdTriggersImplicitOwnerRepo() {
		RegistryEntry entry = resourceRegistry.getEntry(RelationshipBehaviorTestResource.class);
		Object relRepository = entry.getRelationshipRepositoryForType("schedules", null)
				.getRelationshipRepository();
		Assert.assertEquals(ImplicitOwnerBasedRelationshipRepository.class, relRepository.getClass());
	}

	@Test
	public void checkNoLookupTriggersImplicitOwnerRepo() {
		RegistryEntry entry = resourceRegistry.getEntry(RelationshipBehaviorTestResource.class);
		Object relRepository = entry.getRelationshipRepositoryForType("lazy_tasks", null)
				.getRelationshipRepository();
		Assert.assertEquals(ImplicitOwnerBasedRelationshipRepository.class, relRepository.getClass());
	}

	@Test(expected = RelationshipRepositoryNotFoundException.class)
	public void checkAlwaysLookupTriggersNoImplicitRepo() {
		RegistryEntry entry = resourceRegistry.getEntry(RelationshipBehaviorTestResource.class);
		Object relRepository = entry.getRelationshipRepositoryForType("projects-polymorphic", null);
		Assert.assertNull(relRepository);
	}

	@Test
	public void checkImplicitOwnerRepo() {
		RegistryEntry entry = resourceRegistry.getEntry(RelationshipBehaviorTestResource.class);
		Object relRepository = entry.getRelationshipRepositoryForType("projects", null)
				.getRelationshipRepository();
		Assert.assertEquals(ImplicitOwnerBasedRelationshipRepository.class, relRepository.getClass());
	}

	@Test
	public void checkImplicitGetOppositeModifyOwner() {
		RegistryEntry entry = resourceRegistry.getEntry(RelationshipBehaviorTestResource.class);
		Object relRepository = entry.getRelationshipRepositoryForType("tasks", null)
				.getRelationshipRepository();
		Assert.assertEquals(RelationshipRepositoryBase.class, relRepository.getClass());
	}
}
