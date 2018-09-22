package io.crnk.core.repository.forward;

import io.crnk.core.CoreTestContainer;
import io.crnk.core.engine.http.HttpRequestContextProvider;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.mock.models.Project;
import io.crnk.core.mock.models.RelationIdTestResource;
import io.crnk.core.mock.models.Schedule;
import io.crnk.core.mock.models.Task;
import io.crnk.core.mock.repository.MockRepositoryUtil;
import io.crnk.core.mock.repository.ProjectRepository;
import io.crnk.core.mock.repository.RelationIdTestRepository;
import io.crnk.core.mock.repository.ScheduleRepositoryImpl;
import io.crnk.core.mock.repository.TaskRepository;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.RelationshipMatcher;
import io.crnk.core.repository.foward.ForwardingDirection;
import io.crnk.core.repository.foward.ForwardingRelationshipRepository;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class OppositeFowardingRelationshipRepositoryTest {


	private ForwardingRelationshipRepository relRepository;

	private RelationIdTestRepository testRepository;

	private ResourceRegistry resourceRegistry;

	private HttpRequestContextProvider requestContextProvider;

	@Before
	public void setup() {
		MockRepositoryUtil.clear();

		CoreTestContainer container = new CoreTestContainer();
		container.setDefaultPackage();
		container.boot();
		resourceRegistry = container.getResourceRegistry();


		testRepository = (RelationIdTestRepository) container.getEntry(RelationIdTestResource.class)
				.getResourceRepository().getResourceRepository();
		requestContextProvider = container.getModuleRegistry().getHttpRequestContextProvider();

		RelationshipMatcher relMatcher =
				new RelationshipMatcher().rule().source(RelationIdTestResource.class).target(RelationIdTestResource.class).add();
		relRepository = new ForwardingRelationshipRepository(RelationIdTestResource.class, relMatcher,
				ForwardingDirection.OPPOSITE, ForwardingDirection.OPPOSITE);
		relRepository.setResourceRegistry(container.getResourceRegistry());
		relRepository.setHttpRequestContextProvider(requestContextProvider);
	}

	@Test
	public void checkFindOneTarget() {
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
	public void checkFindOneTargetWithRelationId() {
		RelationIdTestResource parent = new RelationIdTestResource();
		parent.setId(2L);
		parent.setName("parent");

		RelationIdTestResource child = new RelationIdTestResource();
		child.setId(3L);
		child.setName("child");

		parent.setTestNestedId(child.getId());
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
		relRepository.setHttpRequestContextProvider(requestContextProvider);

		QuerySpec querySpec = new QuerySpec(Task.class);
		List<Task> tasks = relRepository.findManyTargets(42L, "tasks", querySpec);
		Assert.assertEquals(1, tasks.size());
		Assert.assertEquals(13L, tasks.get(0).getId().longValue());
	}

	@Test
	public void checkFindManyTargetsWithRelationId() {
		ProjectRepository projectRepository = new ProjectRepository();
		Project project = new Project();
		project.setId(42L);
		project.setName("project");
		projectRepository.save(project);

		ScheduleRepositoryImpl scheduleRepository = new ScheduleRepositoryImpl();
		Schedule schedule = new Schedule();
		schedule.setId(13L);
		schedule.setName("schedule");
		schedule.setProjectId(project.getId());
		scheduleRepository.save(schedule);

		relRepository = new ForwardingRelationshipRepository(Project.class, null,
				ForwardingDirection.OPPOSITE, ForwardingDirection.OPPOSITE);
		relRepository.setResourceRegistry(resourceRegistry);
		relRepository.setHttpRequestContextProvider(requestContextProvider);

		QuerySpec querySpec = new QuerySpec(Schedule.class);
		List<Schedule> schedules = relRepository.findManyTargets(42L, "schedules", querySpec);
		Assert.assertEquals(1, schedules.size());
		Assert.assertEquals(13L, schedules.get(0).getId().longValue());
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
		relRepository.setHttpRequestContextProvider(requestContextProvider);

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
		relRepository.setHttpRequestContextProvider(requestContextProvider);

		QuerySpec querySpec = new QuerySpec(Task.class);
		try {
			relRepository.findOneTarget(13L, "project", querySpec);
			Assert.fail();
		} catch (IllegalStateException e) {
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

		relRepository = new ForwardingRelationshipRepository(Task.class, null,
				ForwardingDirection.OPPOSITE, ForwardingDirection.OPPOSITE);
		relRepository.setResourceRegistry(resourceRegistry);
		relRepository.setHttpRequestContextProvider(requestContextProvider);

		QuerySpec querySpec = new QuerySpec(Task.class);
		try {
			relRepository.findOneTarget(13L, "project", querySpec);
			Assert.fail();
		} catch (IllegalStateException e) {
			Assert.assertTrue(e.getMessage(), e.getMessage().contains("To make use of opposite forwarding behavior for resource lookup"));
		}
	}

	@Test
	public void checkFindTargetWithNullRelationshipValue() {
		TaskRepository taskRepository = new TaskRepository();
		Task task = new Task();
		task.setId(13L);
		task.setName("task");
		taskRepository.save(task);

		Task nullIdTask = new Task();

		ProjectRepository projectRepository = new ProjectRepository();
		Project project = new Project();
		project.setId(42L);
		project.setName("project");
		project.setTasks(Arrays.asList(nullIdTask));
		projectRepository.save(project);

		relRepository = new ForwardingRelationshipRepository(Task.class, null,
				ForwardingDirection.OPPOSITE, ForwardingDirection.OPPOSITE);
		relRepository.setResourceRegistry(resourceRegistry);
		relRepository.setHttpRequestContextProvider(requestContextProvider);

		QuerySpec querySpec = new QuerySpec(Task.class);
		try {
			relRepository.findOneTarget(13L, "project", querySpec);
			Assert.fail();
		} catch (IllegalStateException e) {
			Assert.assertTrue(e.getMessage().contains("id is null for"));
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
