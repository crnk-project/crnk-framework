package io.crnk.core.repository.forward;

import java.util.Arrays;
import java.util.List;

import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.internal.utils.CoreClassTestUtils;
import io.crnk.core.engine.internal.utils.MultivaluedMap;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.engine.url.ConstantServiceUrlProvider;
import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.core.mock.MockConstants;
import io.crnk.core.mock.models.Project;
import io.crnk.core.mock.models.RelationIdTestResource;
import io.crnk.core.mock.models.Schedule;
import io.crnk.core.mock.models.Task;
import io.crnk.core.mock.repository.MockRepositoryUtil;
import io.crnk.core.mock.repository.ProjectRepository;
import io.crnk.core.mock.repository.RelationIdTestRepository;
import io.crnk.core.mock.repository.ScheduleRepositoryImpl;
import io.crnk.core.mock.repository.TaskRepository;
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

public class OwnerFowardingRelationshipRepositoryTest {


	private ForwardingRelationshipRepository relRepository;

	private ScheduleRepositoryImpl scheduleRepository;

	private Schedule schedule3;

	private RelationIdTestRepository testRepository;

	private RelationIdTestResource resource;

	private Schedule schedule;

	private ProjectRepository projectRepository;

	private Project project;

	private TaskRepository taskRepository;

	private Task task;

	private ForwardingRelationshipRepository taskProjectRepository;

	private ResourceRegistry resourceRegistry;

	@Before
	public void setup() {
		MockRepositoryUtil.clear();

		CrnkBoot boot = new CrnkBoot();
		boot.setServiceDiscovery(new ReflectionsServiceDiscovery(MockConstants.TEST_MODELS_PACKAGE));
		boot.setServiceUrlProvider(new ConstantServiceUrlProvider(ResourceRegistryTest.TEST_MODELS_URL));
		boot.boot();

		resourceRegistry = boot.getResourceRegistry();

		RegistryEntry entry = resourceRegistry.getEntry(RelationIdTestResource.class);
		relRepository =
				(ForwardingRelationshipRepository) entry.getRelationshipRepository("testSerializeEager", null)
						.getRelationshipRepository();

		RelationshipMatcher taskProjectMatcher = new RelationshipMatcher().rule().source(Task.class).target(Project.class).add();
		taskProjectRepository = new ForwardingRelationshipRepository(Task.class, taskProjectMatcher, ForwardingDirection.OWNER,
				ForwardingDirection.OWNER);
		taskProjectRepository.setResourceRegistry(resourceRegistry);

		testRepository = (RelationIdTestRepository) entry.getResourceRepository().getResourceRepository();
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

		for (int i = 0; i < 10; i++) {
			schedule = new Schedule();
			schedule.setId(4L + i);
			schedule.setName("schedule");
			scheduleRepository.create(schedule);

			projectRepository = new ProjectRepository();
			project = new Project();
			project.setId(42L + i);
			project.setName("project");
			projectRepository.save(project);

			taskRepository = new TaskRepository();
			task = new Task();
			task.setId(13L + i);
			task.setName("task");
			taskRepository.save(task);
		}
	}

	@Test
	public void hasProtectedDefaultConstructor() {
		CoreClassTestUtils.assertProtectedConstructor(ForwardingRelationshipRepository.class);
	}

	@Test
	public void checkSetRelationId() {
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


	@Test(expected = ResourceNotFoundException.class)
	public void checkSetRelationIdNotFound() {
		relRepository.setRelation(resource, 123123L, "testSerializeEager");
		Assert.assertEquals(123123L, resource.getTestSerializeEagerId().longValue());
		Assert.assertNull(resource.getTestSerializeEager());
		relRepository.findOneTarget(resource.getId(), "testSerializeEager", new QuerySpec(Schedule.class));
	}


	@Test
	public void checkSetRelationIdToNull() {
		relRepository.setRelation(resource, null, "testSerializeEager");
		Assert.assertEquals(null, resource.getTestSerializeEagerId());
		Assert.assertNull(resource.getTestSerializeEager());
		Assert.assertNull(relRepository.findOneTarget(resource.getId(), "testSerializeEager", new QuerySpec(Schedule.class)));
	}

	@Test
	public void checkSetRelationIds() {
		relRepository.setRelations(resource, Arrays.asList(3L, 4L), "testMultipleValues");
		Assert.assertEquals(Arrays.asList(3L, 4L), resource.getTestMultipleValueIds());

		List<Schedule> targets =
				relRepository.findManyTargets(resource.getId(), "testMultipleValues", new QuerySpec(Schedule.class));
		Assert.assertEquals(2, targets.size());
		Assert.assertSame(schedule3, targets.get(0));
		Assert.assertSame(4L, targets.get(1).getId().longValue());

		MultivaluedMap targetsMap =
				relRepository.findTargets(Arrays.asList(resource.getId()), "testMultipleValues", new QuerySpec(Schedule.class));
		Assert.assertEquals(1, targetsMap.keySet().size());
		targets = targetsMap.getList(resource.getId());
		Assert.assertEquals(2, targets.size());
		Assert.assertSame(3L, targets.get(0).getId().longValue());
		Assert.assertSame(4L, targets.get(1).getId().longValue());
	}

	@Test
	public void checkAddRemoveRelationIds() {
		relRepository.addRelations(resource, Arrays.asList(3L, 4L), "testMultipleValues");
		Assert.assertEquals(Arrays.asList(3L, 4L), resource.getTestMultipleValueIds());

		relRepository.addRelations(resource, Arrays.asList(5L), "testMultipleValues");
		Assert.assertEquals(Arrays.asList(3L, 4L, 5L), resource.getTestMultipleValueIds());

		relRepository.removeRelations(resource, Arrays.asList(3L), "testMultipleValues");
		Assert.assertEquals(Arrays.asList(4L, 5L), resource.getTestMultipleValueIds());
	}

	@Test
	public void checkSetRelation() {
		taskProjectRepository.setRelation(task, 42L, "project");
		Assert.assertEquals(42L, task.getProject().getId().longValue());

		Project target = (Project) taskProjectRepository.findOneTarget(task.getId(), "project", new QuerySpec(Task.class));
		Assert.assertSame(42L, target.getId().longValue());
	}

	@Test
	public void checkSetRelations() {
		taskProjectRepository.setRelations(task, Arrays.asList(42L), "projects");
		Assert.assertEquals(1, task.getProjects().size());
		Assert.assertEquals(42L, task.getProjects().iterator().next().getId().longValue());

		MultivaluedMap targets =
				taskProjectRepository.findTargets(Arrays.asList(task.getId()), "projects", new QuerySpec(Task.class));
		Assert.assertEquals(1, targets.keySet().size());
		Assert.assertEquals(task.getId(), targets.keySet().iterator().next());
		Project target = (Project) targets.getUnique(task.getId());
		Assert.assertEquals(42L, target.getId().longValue());
	}


	@After
	public void teardown() {
		MockRepositoryUtil.clear();
	}

}
