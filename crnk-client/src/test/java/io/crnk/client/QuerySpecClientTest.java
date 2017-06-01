package io.crnk.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.crnk.client.http.HttpAdapter;
import io.crnk.client.http.okhttp.OkHttpAdapter;
import io.crnk.client.http.okhttp.OkHttpAdapterListener;
import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.core.queryspec.Direction;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.queryspec.SortSpec;
import io.crnk.core.repository.RelationshipRepositoryV2;
import io.crnk.core.repository.ResourceRepositoryV2;
import io.crnk.test.mock.models.Project;
import io.crnk.test.mock.models.Schedule;
import io.crnk.test.mock.models.Task;
import io.crnk.test.mock.repository.ScheduleRepository;
import io.crnk.test.mock.repository.ScheduleRepository.ScheduleList;
import io.crnk.test.mock.repository.ScheduleRepository.ScheduleListLinks;
import io.crnk.test.mock.repository.ScheduleRepository.ScheduleListMeta;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient.Builder;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class QuerySpecClientTest extends AbstractClientTest {

	protected ResourceRepositoryV2<Task, Long> taskRepo;

	protected ResourceRepositoryV2<Project, Long> projectRepo;

	protected ResourceRepositoryV2<Schedule, Long> scheduleRepo;

	protected RelationshipRepositoryV2<Task, Long, Project, Long> relRepo;

	protected RelationshipRepositoryV2<Schedule, Long, Task, Long> scheduleTaskRepo;

	protected RelationshipRepositoryV2<Task, Long, Schedule, Long> taskScheduleRepo;

	@Before
	public void setup() {
		super.setup();

		scheduleRepo = client.getQuerySpecRepository(Schedule.class);
		taskRepo = client.getQuerySpecRepository(Task.class);
		projectRepo = client.getQuerySpecRepository(Project.class);
		relRepo = client.getQuerySpecRepository(Task.class, Project.class);
		scheduleTaskRepo = client.getQuerySpecRepository(Schedule.class, Task.class);
		taskScheduleRepo = client.getQuerySpecRepository(Task.class, Schedule.class);
	}

	@Test
	public void testGetters() {
		Assert.assertEquals(Task.class, taskRepo.getResourceClass());
		Assert.assertEquals(Task.class, relRepo.getSourceResourceClass());
		Assert.assertEquals(Project.class, relRepo.getTargetResourceClass());
	}

	@Override
	protected TestApplication configure() {
		return new TestApplication(true);
	}

	@Test
	public void testInterfaceAccess() {
		// tag::interfaceAccess[]
		ScheduleRepository scheduleRepository = client.getResourceRepository(ScheduleRepository.class);

		Schedule schedule = new Schedule();
		schedule.setId(13L);
		schedule.setName("mySchedule");
		scheduleRepository.create(schedule);

		QuerySpec querySpec = new QuerySpec(Schedule.class);
		ScheduleList list = scheduleRepository.findAll(querySpec);
		Assert.assertEquals(1, list.size());
		ScheduleListMeta meta = list.getMeta();
		ScheduleListLinks links = list.getLinks();
		Assert.assertNotNull(meta);
		Assert.assertNotNull(links);
		// end::interfaceAccess[]
	}

	@Test
	public void testCreate() {
		ScheduleRepository scheduleRepository = client.getResourceRepository(ScheduleRepository.class);

		Schedule schedule = new Schedule();
		schedule.setName("mySchedule");
		scheduleRepository.create(schedule);

		QuerySpec querySpec = new QuerySpec(Schedule.class);
		ScheduleList list = scheduleRepository.findAll(querySpec);
		Assert.assertEquals(1, list.size());
		schedule = list.get(0);
		Assert.assertNotNull(schedule.getId());
		ScheduleListMeta meta = list.getMeta();
		ScheduleListLinks links = list.getLinks();
		Assert.assertNotNull(meta);
		Assert.assertNotNull(links);
	}

	@Test
	public void testSortAsc() {
		for (int i = 0; i < 5; i++) {
			Task task = new Task();
			task.setId(Long.valueOf(i));
			task.setName("task" + i);
			taskRepo.create(task);
		}
		QuerySpec querySpec = new QuerySpec(Task.class);
		querySpec.addSort(new SortSpec(Arrays.asList("name"), Direction.ASC));
		List<Task> tasks = taskRepo.findAll(querySpec);
		Assert.assertEquals(5, tasks.size());
		for (int i = 0; i < 5; i++) {
			Assert.assertEquals("task" + i, tasks.get(i).getName());
		}
	}

	@Test
	public void testSortDesc() {
		for (int i = 0; i < 5; i++) {
			Task task = new Task();
			task.setId(Long.valueOf(i));
			task.setName("task" + i);
			taskRepo.create(task);
		}
		QuerySpec querySpec = new QuerySpec(Task.class);
		querySpec.addSort(new SortSpec(Arrays.asList("name"), Direction.DESC));
		List<Task> tasks = taskRepo.findAll(querySpec);
		Assert.assertEquals(5, tasks.size());
		for (int i = 0; i < 5; i++) {
			Assert.assertEquals("task" + i, tasks.get(4 - i).getName());
		}
	}

	@Test
	public void testFindEmpty() {
		List<Task> tasks = taskRepo.findAll(new QuerySpec(Task.class));
		Assert.assertTrue(tasks.isEmpty());
	}

	@Test(expected = ResourceNotFoundException.class)
	public void testFindNull() {
		taskRepo.findOne(1L, new QuerySpec(Task.class));
	}

	@Test
	public void testCreateAndFind() {
		Task task = new Task();
		task.setId(1L);
		task.setName("test");
		taskRepo.create(task);

		// check retrievable with findAll
		List<Task> tasks = taskRepo.findAll(new QuerySpec(Task.class));
		Assert.assertEquals(1, tasks.size());
		Task savedTask = tasks.get(0);
		Assert.assertEquals(task.getId(), savedTask.getId());
		Assert.assertEquals(task.getName(), savedTask.getName());

		// check retrievable with findAll(ids)
		tasks = taskRepo.findAll(Arrays.asList(1L), new QuerySpec(Task.class));
		Assert.assertEquals(1, tasks.size());
		savedTask = tasks.get(0);
		Assert.assertEquals(task.getId(), savedTask.getId());
		Assert.assertEquals(task.getName(), savedTask.getName());

		// check retrievable with findOne
		savedTask = taskRepo.findOne(1L, new QuerySpec(Task.class));
		Assert.assertEquals(task.getId(), savedTask.getId());
		Assert.assertEquals(task.getName(), savedTask.getName());
	}

	@Test
	public void testUpdatePushAlways() {
		client.setPushAlways(true);
		testUpdate(true);
	}

	@Test
	public void testUpdate() {
		client.setPushAlways(false);
		testUpdate(false);
	}

	public void testUpdate(boolean pushAlways) {
		final List<String> methods = new ArrayList<>();
		final List<String> paths = new ArrayList<>();
		final Interceptor interceptor = new Interceptor() {

			@Override
			public Response intercept(Chain chain) throws IOException {
				Request request = chain.request();

				methods.add(request.method());
				paths.add(request.url().encodedPath());

				return chain.proceed(request);
			}
		};

		HttpAdapter httpAdapter = client.getHttpAdapter();
		if (httpAdapter instanceof OkHttpAdapter) {
			((OkHttpAdapter) httpAdapter).addListener(new OkHttpAdapterListener() {

				@Override
				public void onBuild(Builder builder) {
					builder.addInterceptor(interceptor);
				}
			});
		}

		Task task = new Task();
		task.setId(1L);
		task.setName("test");
		taskRepo.create(task);

		Task savedTask = taskRepo.findOne(1L, new QuerySpec(Task.class));
		Assert.assertNotNull(savedTask);

		// perform update
		task.setName("updatedName");
		taskRepo.save(task);

		// check updated
		savedTask = taskRepo.findOne(1L, new QuerySpec(Task.class));
		Assert.assertNotNull(savedTask);
		Assert.assertEquals("updatedName", task.getName());

		if (httpAdapter instanceof OkHttpAdapter) {
			// check HTTP handling
			Assert.assertEquals(4, methods.size());
			Assert.assertEquals(4, paths.size());
			Assert.assertEquals("POST", methods.get(0));
			Assert.assertEquals("GET", methods.get(1));
			if (pushAlways) {
				Assert.assertEquals("POST", methods.get(2));
				Assert.assertEquals("/tasks/", paths.get(2));
			}
			else {
				Assert.assertEquals("PATCH", methods.get(2));
				Assert.assertEquals("/tasks/1/", paths.get(2));
			}
			Assert.assertEquals("GET", methods.get(3));

			Assert.assertEquals("/tasks/", paths.get(0));
			Assert.assertEquals("/tasks/1/", paths.get(1));
			Assert.assertEquals("/tasks/1/", paths.get(3));
		}
	}

	@Test
	public void testGeneratedId() {
		Task task = new Task();
		task.setId(null);
		task.setName("test");
		Task savedTask = taskRepo.create(task);
		Assert.assertNotNull(savedTask.getId());
	}

	@Test
	public void testDelete() {
		Task task = new Task();
		task.setId(1L);
		task.setName("test");
		taskRepo.create(task);

		taskRepo.delete(1L);

		List<Task> tasks = taskRepo.findAll(new QuerySpec(Task.class));
		Assert.assertEquals(0, tasks.size());
	}

	@Test
	public void testSetRelation() {
		Schedule schedule = new Schedule();
		schedule.setId(1L);
		schedule.setName("schedule");
		scheduleRepo.create(schedule);

		Task task = new Task();
		task.setId(2L);
		task.setName("test");
		taskRepo.create(task);

		relRepo.setRelation(task, schedule.getId(), "schedule");

		Schedule relSchedule = taskScheduleRepo.findOneTarget(task.getId(), "schedule", new QuerySpec(Schedule.class));
		Assert.assertNotNull(relSchedule);
		Assert.assertEquals(schedule.getId(), relSchedule.getId());
	}

	@Test
	public void testSaveRelationWithCreate() {
		Schedule schedule = new Schedule();
		schedule.setId(1L);
		schedule.setName("schedule");
		scheduleRepo.create(schedule);

		Task task = new Task();
		task.setId(2L);
		task.setName("test");
		task.setSchedule(schedule);
		taskRepo.create(task);

		// check relationship available
		Task savedTask = taskRepo.findOne(task.getId(), new QuerySpec(Task.class));
		Assert.assertNotNull(savedTask.getSchedule());
	}

	@Test
	public void testNullNonLazyRelationWithSave() {
		Schedule schedule = new Schedule();
		schedule.setId(1L);
		schedule.setName("schedule");
		scheduleRepo.create(schedule);

		Task task = new Task();
		task.setId(2L);
		task.setName("test");
		task.setSchedule(schedule);
		taskRepo.create(task);

		Task savedTask = taskRepo.findOne(task.getId(), new QuerySpec(Task.class));
		Assert.assertNotNull(savedTask.getSchedule());

		// null
		savedTask.setSchedule(null);
		taskRepo.save(savedTask);

		// relation must be null
		Task updatedTask = taskRepo.findOne(task.getId(), new QuerySpec(Task.class));
		Assert.assertNull(updatedTask.getSchedule());
	}

	@Test
	public void testCannotNullLazyRelationWithSave() {
		Task task = new Task();
		task.setId(2L);
		task.setName("test");
		taskRepo.create(task);

		Schedule schedule = new Schedule();
		schedule.setId(1L);
		schedule.setName("schedule");
		schedule.setLazyTask(task);
		scheduleRepo.create(schedule);

		// since lazy, will not be sent to client if not requested
		QuerySpec querySpec = new QuerySpec(Schedule.class);
		Schedule savedSchedule = scheduleRepo.findOne(schedule.getId(), querySpec);
		Assert.assertNull(savedSchedule.getLazyTask());

		querySpec.includeRelation(Arrays.asList("lazyTask"));
		savedSchedule = scheduleRepo.findOne(schedule.getId(), querySpec);
		Assert.assertNotNull(savedSchedule.getLazyTask());

		// null
		savedSchedule.setLazyTask(task);
		scheduleRepo.save(savedSchedule);

		// still not null because cannot differantiate between not loaded and
		// nulled
		Schedule updatedSchedule = scheduleRepo.findOne(schedule.getId(), querySpec);
		Assert.assertNotNull(updatedSchedule.getLazyTask());
	}

	@Test
	public void testSaveRelationWithSave() {
		Schedule schedule = new Schedule();
		schedule.setId(1L);
		schedule.setName("schedule");
		scheduleRepo.create(schedule);

		Task task = new Task();
		task.setId(2L);
		task.setName("test");
		taskRepo.create(task);

		Task createdTask = taskRepo.findOne(task.getId(), new QuerySpec(Task.class));
		Assert.assertNull(createdTask.getSchedule());
		createdTask.setSchedule(schedule);
		taskRepo.save(createdTask);

		Task updatedTask = taskRepo.findOne(task.getId(), new QuerySpec(Task.class));
		Assert.assertNotNull(updatedTask.getSchedule());
	}

	@Test
	public void testAddSetRemoveRelations() {
		Project project0 = new Project();
		project0.setId(1L);
		project0.setName("project0");
		projectRepo.create(project0);

		Project project1 = new Project();
		project1.setId(2L);
		project1.setName("project1");
		projectRepo.create(project1);

		Task task = new Task();
		task.setId(3L);
		task.setName("test");
		taskRepo.create(task);

		relRepo.addRelations(task, Arrays.asList(project0.getId(), project1.getId()), "projects");
		List<Project> relProjects = relRepo.findManyTargets(task.getId(), "projects", new QuerySpec(Task.class));
		Assert.assertEquals(2, relProjects.size());

		relRepo.setRelations(task, Arrays.asList(project1.getId()), "projects");
		relProjects = relRepo.findManyTargets(task.getId(), "projects", new QuerySpec(Task.class));
		Assert.assertEquals(1, relProjects.size());
		Assert.assertEquals(project1.getId(), relProjects.get(0).getId());


		// TODO HTTP DELETE method with payload not supported? at least in
		// Jersey
		/*
		relRepo.removeRelations(task, Arrays.asList(project1.getId()),
				"projects");
		relProjects = relRepo.findManyTargets(task.getId(), "projects", new QuerySpec(Task.class));
		Assert.assertEquals(0, relProjects.size());
		*/
	}
}
