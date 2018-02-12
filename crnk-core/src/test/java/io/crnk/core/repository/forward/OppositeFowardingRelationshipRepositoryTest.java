package io.crnk.core.repository.forward;

import java.util.Arrays;
import java.util.List;

import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.engine.url.ConstantServiceUrlProvider;
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

public class OppositeFowardingRelationshipRepositoryTest {


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
				ForwardingDirection.OPPOSITE, ForwardingDirection.OPPOSITE);
		relRepository.setResourceRegistry(resourceRegistry);
	}

	@Test
	public void checkFindOneTargetFromSingleValue() {
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

	@Test
	public void checkFindManyTargets() {
		ProjectRepository projectRepository = new ProjectRepository();
		Project project = new Project();
		project.setId(42L);
		project.setName("project");
		projectRepository.save(project);

		TaskRepository taskRepository = new TaskRepository();
		Task task = new Task();
		task.setId(13L);
		task.setName("task");
		task.setProject(project);
		taskRepository.save(task);

		relRepository = new ForwardingRelationshipRepository(Project.class, null,
				ForwardingDirection.OPPOSITE, ForwardingDirection.OPPOSITE);
		relRepository.setResourceRegistry(resourceRegistry);

		QuerySpec querySpec = new QuerySpec(Task.class);
		List<Task> tasks = relRepository.findManyTargets(42L, "tasks", querySpec);
		Assert.assertEquals(1, tasks.size());
		Assert.assertEquals(13L, tasks.get(0).getId().longValue());
	}


	@Test
	public void checkFindOneTargetFromCollection() {
		TaskRepository taskRepository = new TaskRepository();
		Task task = new Task();
		task.setId(13L);
		task.setName("task");
		taskRepository.save(task);

		ProjectRepository projectRepository = new ProjectRepository();
		Project project = new Project();
		project.setId(42L);
		project.setName("project");
		project.setTasks(Arrays.asList(task));
		projectRepository.save(project);

		relRepository = new ForwardingRelationshipRepository(Task.class, null,
				ForwardingDirection.OPPOSITE, ForwardingDirection.OPPOSITE);
		relRepository.setResourceRegistry(resourceRegistry);

		QuerySpec querySpec = new QuerySpec(Task.class);
		Project foundProject = (Project) relRepository.findOneTarget(13L, "project", querySpec);
		Assert.assertEquals(42L, foundProject.getId().longValue());
	}

	@Test
	public void checkFindTargetWithInvalidNullReturnId() {
		TaskRepository taskRepository = new TaskRepository();
		Task task = new Task();
		task.setId(13L);
		task.setName("task");
		taskRepository.save(task);

		ProjectRepository projectRepository = new ProjectRepository();
		Project project = new Project();
		project.setId(42L);
		project.setName("project");
		project.setTasks(Arrays.asList(task));
		projectRepository.save(project);

		// manipulate
		task.setId(null);

		relRepository = new ForwardingRelationshipRepository(Task.class, null,
				ForwardingDirection.OPPOSITE, ForwardingDirection.OPPOSITE);
		relRepository.setResourceRegistry(resourceRegistry);

		QuerySpec querySpec = new QuerySpec(Task.class);
		try {
			relRepository.findOneTarget(13L, "project", querySpec);
			Assert.fail();
		}
		catch (IllegalStateException e) {
			Assert.assertTrue(e.getMessage().contains("id is null"));
		}
	}

	@Test
	public void checkFindTargetWithNotLoadedRelationship() {
		TaskRepository taskRepository = new TaskRepository();
		Task task = new Task();
		task.setId(13L);
		task.setName("task");
		taskRepository.save(task);

		ProjectRepository projectRepository = new ProjectRepository();
		Project project = new Project();
		project.setId(42L);
		project.setName("project");
		project.setTasks(null);
		projectRepository.save(project);

		// manipulate
		task.setId(null);

		relRepository = new ForwardingRelationshipRepository(Task.class, null,
				ForwardingDirection.OPPOSITE, ForwardingDirection.OPPOSITE);
		relRepository.setResourceRegistry(resourceRegistry);

		QuerySpec querySpec = new QuerySpec(Task.class);
		try {
			relRepository.findOneTarget(13L, "project", querySpec);
			Assert.fail();
		}
		catch (IllegalStateException e) {
			Assert.assertTrue(e.getMessage().contains("field tasks is null for"));
		}
	}


	@Test(expected = UnsupportedOperationException.class)
	public void checkSetRelationNotYetImplemented() {
		relRepository.setRelation(null, null, null);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void checkSetRelationsNotYetImplemented() {
		relRepository.setRelations(null, null, null);
	}


	@Test(expected = UnsupportedOperationException.class)
	public void checkAddRelationsNotYetImplemented() {
		relRepository.addRelations(null, null, null);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void checkRemoveRelationsNotYetImplemented() {
		relRepository.addRelations(null, null, null);
	}

	@After
	public void teardown() {
		MockRepositoryUtil.clear();
	}

}
