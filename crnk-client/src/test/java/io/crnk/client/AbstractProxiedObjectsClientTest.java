package io.crnk.client;

import java.io.Serializable;
import java.util.Collection;

import io.crnk.client.internal.proxy.ObjectProxy;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.RelationshipRepositoryV2;
import io.crnk.core.repository.ResourceRepositoryV2;
import io.crnk.test.mock.models.Project;
import io.crnk.test.mock.models.Schedule;
import io.crnk.test.mock.models.Task;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public abstract class AbstractProxiedObjectsClientTest extends AbstractClientTest {

	protected ResourceRepositoryV2<Task, Long> taskRepo;

	protected ResourceRepositoryV2<Project, Long> projectRepo;

	protected RelationshipRepositoryV2<Task, Long, Project, Long> relRepo;

	private ResourceRepositoryV2<Schedule, Long> scheduleRepo;

	private RelationshipRepositoryV2<Schedule, Serializable, Task, Serializable> scheduleTaskRepo;

	private RelationshipRepositoryV2<Task, Serializable, Schedule, Serializable> taskScheduleRepo;

	@Before
	public void setup() {
		super.setup();

		client.setPushAlways(false);

		scheduleRepo = client.getQuerySpecRepository(Schedule.class);
		taskRepo = client.getRepositoryForType(Task.class);
		projectRepo = client.getRepositoryForType(Project.class);
		relRepo = client.getRepositoryForType(Task.class, Project.class);
		scheduleTaskRepo = client.getQuerySpecRepository(Schedule.class, Task.class);
		taskScheduleRepo = client.getRepositoryForType(Task.class, Schedule.class);
	}

	@Override
	protected TestApplication configure() {
		return new TestApplication(false);
	}

	@Test
	public void noProxyForLazy() {
		Schedule schedule = new Schedule();
		schedule.setId(1L);
		schedule.setName("project");
		scheduleRepo.create(schedule);

		Task task = new Task();
		task.setId(2L);
		task.setName("test");
		taskRepo.create(task);

		scheduleTaskRepo.setRelation(schedule, task.getId(), "lazyTask");

		QuerySpec querySpec = new QuerySpec(Task.class);
		schedule = scheduleRepo.findOne(1L, querySpec);
		Task proxiedObject = schedule.getLazyTask();
		Assert.assertNull(proxiedObject);

	}

	@Test
	public void proxyForNoneLazy() {
		Schedule schedule = new Schedule();
		schedule.setId(1L);
		schedule.setName("project");
		scheduleRepo.create(schedule);

		Task task = new Task();
		task.setId(2L);
		task.setName("test");
		taskRepo.create(task);

		scheduleTaskRepo.setRelation(schedule, task.getId(), "task");

		QuerySpec querySpec = new QuerySpec(Task.class);
		schedule = scheduleRepo.findOne(1L, querySpec);
		Task proxiedObject = schedule.getTask();
		Assert.assertNotNull(proxiedObject);
		Assert.assertEquals(2L, proxiedObject.getId().longValue());
		Assert.assertNull(proxiedObject.getName());

	}

	@Test
	public void proxyForLazySet() {
		proxyForCollection(true);
	}

	@Test
	public void proxyForLazyList() {
		proxyForCollection(false);
	}

	private void proxyForCollection(boolean set) {
		Schedule schedule = new Schedule();
		schedule.setId(1L);
		schedule.setName("project");
		scheduleRepo.create(schedule);

		Task task = new Task();
		task.setId(2L);
		task.setName("test");
		taskRepo.create(task);

		taskScheduleRepo.setRelation(task, schedule.getId(), "schedule");

		// collection must be available as proxy
		QuerySpec querySpec = new QuerySpec(Task.class);
		schedule = scheduleRepo.findOne(1L, querySpec);
		Collection<Task> proxiedTasks = set ? schedule.getTasks() : schedule.getTasksList();
		Assert.assertNotNull(proxiedTasks);

		// check status without loading
		ObjectProxy proxy = (ObjectProxy) proxiedTasks;
		Assert.assertFalse(proxy.isLoaded());
		Assert.assertNotNull(proxy.getUrl());
		Assert.assertFalse(proxy.isLoaded());

		// lazy load
		Assert.assertEquals(1, proxiedTasks.size());
		Assert.assertTrue(proxy.isLoaded());
		task = proxiedTasks.iterator().next();
		Assert.assertEquals(2L, task.getId().longValue());
	}

	@Test
	public void saveDoesNotTriggerLazyLoad() {
		Schedule schedule = new Schedule();
		schedule.setId(1L);
		schedule.setName("project");
		scheduleRepo.create(schedule);

		QuerySpec querySpec = new QuerySpec(Schedule.class);
		schedule = scheduleRepo.findOne(1L, querySpec);
		Collection<Task> proxiedTasks = schedule.getTasks();
		ObjectProxy proxy = (ObjectProxy) proxiedTasks;
		Assert.assertFalse(proxy.isLoaded());

		// update primitive field
		schedule.setName("newValue");
		scheduleRepo.save(schedule);

		// save should not trigger a load of the relation
		Assert.assertFalse(proxy.isLoaded());
		Assert.assertSame(proxy, schedule.getTasks());

		// data should be saved
		schedule = scheduleRepo.findOne(1L, querySpec);
		Assert.assertEquals("newValue", schedule.getName());
	}

	@Test
	public void saveLazyCollectionChange() {
		Schedule schedule = new Schedule();
		schedule.setId(1L);
		schedule.setName("project");
		scheduleRepo.create(schedule);

		Task task = new Task();
		task.setId(2L);
		task.setName("test");
		taskRepo.create(task);

		QuerySpec querySpec = new QuerySpec(Task.class);
		schedule = scheduleRepo.findOne(1L, querySpec);
		Collection<Task> proxiedTasks = schedule.getTasks();
		ObjectProxy proxy = (ObjectProxy) proxiedTasks;
		Assert.assertFalse(proxy.isLoaded());

		// add task to collection
		proxiedTasks.add(task);
		Assert.assertTrue(proxy.isLoaded());
		Assert.assertEquals(1, proxiedTasks.size());
		scheduleRepo.save(schedule);

		schedule = scheduleRepo.findOne(1L, querySpec);
		proxiedTasks = schedule.getTasks();
		Assert.assertEquals(1, proxiedTasks.size());

		// remove task from collection
		proxiedTasks.remove(task);
		Assert.assertEquals(1, proxiedTasks.size());
		scheduleRepo.save(schedule);

		schedule = scheduleRepo.findOne(1L, querySpec);
		proxiedTasks = schedule.getTasks();
		Assert.assertEquals(1, proxiedTasks.size());
	}
}