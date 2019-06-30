package io.crnk.core.engine.internal.repository;

import io.crnk.core.CoreTestContainer;
import io.crnk.core.CoreTestModule;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.mock.models.RelationshipBehaviorTestResource;
import io.crnk.core.repository.foward.ForwardingRelationshipRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class RelationshipRepositoryBehaviorTest {

    private ResourceRegistry resourceRegistry;

    @Before
    public void setup() {
        CoreTestContainer container = new CoreTestContainer();
        container.addModule(new CoreTestModule());
        container.boot();
        resourceRegistry = container.getResourceRegistry();
    }

    @Test
    public void checkRelationIdTriggersImplicitOwnerRepo() {
        RegistryEntry entry = resourceRegistry.getEntry(RelationshipBehaviorTestResource.class);
        Object relRepository = entry.getRelationshipRepository("testRelationId")
                .getRelationshipRepository();
        Assert.assertEquals(ForwardingRelationshipRepository.class, relRepository.getClass());
    }

    @Test
    public void checkNoLookupTriggersImplicitOwnerRepo() {
        RegistryEntry entry = resourceRegistry.getEntry(RelationshipBehaviorTestResource.class);
        Object relRepository = entry.getRelationshipRepository("testNoLookup")
                .getRelationshipRepository();
        Assert.assertEquals(ForwardingRelationshipRepository.class, relRepository.getClass());
    }

    @Test
    public void checkImplicitOwnerRepo() {
        RegistryEntry entry = resourceRegistry.getEntry(RelationshipBehaviorTestResource.class);
        Object relRepository = entry.getRelationshipRepository("testImplicityFromOwner")
                .getRelationshipRepository();
        Assert.assertEquals(ForwardingRelationshipRepository.class, relRepository.getClass());
    }
}
