package io.crnk.core.repository.forward;

import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.engine.url.ConstantServiceUrlProvider;
import io.crnk.core.mock.MockConstants;
import io.crnk.core.mock.models.RelationIdTestResource;
import io.crnk.core.mock.models.Schedule;
import io.crnk.core.mock.repository.MockRepositoryUtil;
import io.crnk.core.mock.repository.RelationIdTestRepository;
import io.crnk.core.mock.repository.ScheduleRepositoryImpl;
import io.crnk.core.module.discovery.ReflectionsServiceDiscovery;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.RelationshipMatcher;
import io.crnk.core.repository.foward.ForwardingDirection;
import io.crnk.core.repository.foward.ForwardingRelationshipRepository;
import io.crnk.core.resource.registry.ResourceRegistryTest;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class GetOppositeFowardingRelationshipRepositoryTest {


	private ForwardingRelationshipRepository relRepository;

	private ScheduleRepositoryImpl scheduleRepository;

	private Schedule schedule3;

	private RelationIdTestRepository testRepository;


	private ResourceRegistry resourceRegistry;

	@Before
	public void setup() {
		MockRepositoryUtil.clear();

		CrnkBoot boot = new CrnkBoot();
		boot.setServiceDiscovery(new ReflectionsServiceDiscovery(MockConstants.TEST_MODELS_PACKAGE));
		boot.setServiceUrlProvider(new ConstantServiceUrlProvider(ResourceRegistryTest.TEST_MODELS_URL));
		boot.boot();

		resourceRegistry = boot.getResourceRegistry();

		testRepository = (RelationIdTestRepository) resourceRegistry.getEntry(RelationIdTestResource.class)
				.getResourceRepository().getResourceRepository();

		RelationshipMatcher relMatcher =
				new RelationshipMatcher().rule().source(RelationIdTestResource.class).target(RelationIdTestResource.class).add();
		relRepository = new ForwardingRelationshipRepository(RelationIdTestResource.class, relMatcher,
				ForwardingDirection.OPPOSITE, ForwardingDirection.OWNER);
		relRepository.setResourceRegistry(resourceRegistry);
	}

	@Test
	public void check() {
		RelationIdTestResource parent = new RelationIdTestResource();
		parent.setId(2L);
		parent.setName("parent");

		RelationIdTestResource child = new RelationIdTestResource();
		child.setId(3L);
		child.setName("child");

		parent.setTestNested(child);
		testRepository.create(parent);
		testRepository.create(child);

		QuerySpec querySpec = new QuerySpec(RelationIdTestResource.class);
		Object target = relRepository.findOneTarget(3L, "testNestedOpposite", querySpec);

		Assert.assertNotNull(target);
	}

	@After
	public void teardown() {
		MockRepositoryUtil.clear();
	}

}
