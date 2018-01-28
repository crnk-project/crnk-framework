package io.crnk.core.engine.internal.repository;

import java.util.Arrays;
import java.util.List;

import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.internal.utils.MultivaluedMap;
import io.crnk.core.engine.registry.RegistryEntry;
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
import io.crnk.core.resource.list.ResourceList;
import io.crnk.core.resource.registry.ResourceRegistryTest;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ImplicitIdBasedRelationshipRepositoryTest {


	private ImplicitIdBasedRelationshipRepository relRepository;

	private ScheduleRepositoryImpl scheduleRepository;

	private Schedule schedule3;

	private RelationIdTestRepository testRepository;

	private RelationIdTestResource resource;

	private Schedule schedule4;

	@Before
	public void setup() {
		MockRepositoryUtil.clear();

		CrnkBoot boot = new CrnkBoot();
		boot.setServiceDiscovery(new ReflectionsServiceDiscovery(MockConstants.TEST_MODELS_PACKAGE));
		boot.setServiceUrlProvider(new ConstantServiceUrlProvider(ResourceRegistryTest.TEST_MODELS_URL));
		boot.boot();

		ResourceRegistry resourceRegistry = boot.getResourceRegistry();
		RegistryEntry sourceEntry = resourceRegistry.getEntry(RelationIdTestResource.class);
		relRepository = new ImplicitIdBasedRelationshipRepository(resourceRegistry,
				sourceEntry.getResourceInformation(), Schedule.class);


		testRepository = new RelationIdTestRepository();
		testRepository.setResourceRegistry(resourceRegistry);
		resource = new RelationIdTestResource();
		resource.setId(2L);
		resource.setName("relationId");
		testRepository.create(resource);


		scheduleRepository = new ScheduleRepositoryImpl();
		schedule3 = new Schedule();
		schedule3.setId(3L);
		schedule3.setName("schedule");
		scheduleRepository.create(schedule3);

		schedule4 = new Schedule();
		schedule4.setId(4L);
		schedule4.setName("schedule");
		scheduleRepository.create(schedule4);
	}


	@Test
	public void checkSetRelation() {
		relRepository.setRelation(resource, 3L, "testSerializeEager");
		Assert.assertEquals(3L, resource.getTestSerializeEagerId().longValue());
		Assert.assertNull(resource.getTestSerializeEager());

		Assert.assertSame(schedule3,
				relRepository.findOneTarget(resource.getId(), "testSerializeEager", new QuerySpec(Schedule.class)));

		MultivaluedMap targets =
				relRepository.findTargets(Arrays.asList(resource.getId()), "testSerializeEager", new QuerySpec(Schedule.class));
		Assert.assertEquals(1, targets.keySet().size());
		Object target = targets.getUnique(resource.getId());
		Assert.assertEquals(schedule3, target);

		relRepository.setRelation(resource, null, "testSerializeEager");
		Assert.assertNull(resource.getTestSerializeEagerId());
		Assert.assertNull(resource.getTestSerializeEager());
	}

	@Test
	public void checkSetRelations() {
		relRepository.setRelations(resource, Arrays.asList(3L, 4L), "testMultipleValues");
		Assert.assertEquals(Arrays.asList(3L, 4L), resource.getTestMultipleValueIds());

		List targets =
				relRepository.findManyTargets(resource.getId(), "testMultipleValues", new QuerySpec(Schedule.class));
		Assert.assertEquals(2, targets.size());
		Assert.assertSame(schedule3, targets.get(0));
		Assert.assertSame(schedule4, targets.get(1));

		MultivaluedMap targetsMap =
				relRepository.findTargets(Arrays.asList(resource.getId()), "testMultipleValues", new QuerySpec(Schedule.class));
		Assert.assertEquals(1, targetsMap.keySet().size());
		targets = targetsMap.getList(resource.getId());
		Assert.assertEquals(2, targets.size());
		Assert.assertSame(schedule3, targets.get(0));
		Assert.assertSame(schedule4, targets.get(1));
	}

	@Test
	public void checkAddRemoveRelations() {
		relRepository.addRelations(resource, Arrays.asList(3L, 4L), "testMultipleValues");
		Assert.assertEquals(Arrays.asList(3L, 4L), resource.getTestMultipleValueIds());

		relRepository.addRelations(resource, Arrays.asList(5L), "testMultipleValues");
		Assert.assertEquals(Arrays.asList(3L, 4L, 5L), resource.getTestMultipleValueIds());

		relRepository.removeRelations(resource, Arrays.asList(3L), "testMultipleValues");
		Assert.assertEquals(Arrays.asList(4L, 5L), resource.getTestMultipleValueIds());
	}


	@After
	public void teardown() {
		MockRepositoryUtil.clear();
	}

}
