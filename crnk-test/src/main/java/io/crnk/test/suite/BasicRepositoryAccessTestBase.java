package io.crnk.test.suite;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.crnk.core.engine.http.HttpHeaders;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.exception.MethodNotAllowedException;
import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.core.queryspec.Direction;
import io.crnk.core.queryspec.FilterOperator;
import io.crnk.core.queryspec.FilterSpec;
import io.crnk.core.queryspec.PathSpec;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.queryspec.SortSpec;
import io.crnk.core.repository.RelationshipRepository;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.core.resource.links.DefaultPagedLinksInformation;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.test.mock.models.HistoricTask;
import io.crnk.test.mock.models.Project;
import io.crnk.test.mock.models.ReadOnlyTask;
import io.crnk.test.mock.models.RelocatedTask;
import io.crnk.test.mock.models.Schedule;
import io.crnk.test.mock.models.Task;
import io.crnk.test.mock.models.TaskSubType;
import io.crnk.test.mock.repository.ProjectRepository;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public abstract class BasicRepositoryAccessTestBase {

	protected TestContainer testContainer;

	protected ResourceRepository<Task, Long> taskRepo;

	protected ResourceRepository<Project, Long> projectRepo;

	protected ResourceRepository<Schedule, Long> scheduleRepo;

	protected RelationshipRepository<Task, Long, Project, Long> relRepo;

	protected RelationshipRepository<Schedule, Long, Task, Long> scheduleTaskRepo;

	protected RelationshipRepository<Task, Long, Schedule, Long> taskScheduleRepo;

	@Before
	public void setup() {
		testContainer.start();
		scheduleRepo = testContainer.getRepositoryForType(Schedule.class);
		taskRepo = testContainer.getRepositoryForType(Task.class);
		projectRepo = testContainer.getRepositoryForType(Project.class);
		relRepo = testContainer.getRepositoryForType(Task.class, Project.class);
		scheduleTaskRepo = testContainer.getRepositoryForType(Schedule.class, Task.class);
		taskScheduleRepo = testContainer.getRepositoryForType(Task.class, Schedule.class);
	}

	@After
	public void tearDown() {
		testContainer.stop();
	}

	@Test
	public void testGetters() {
		Assert.assertEquals(Task.class, taskRepo.getResourceClass());
		Assert.assertEquals(Task.class, relRepo.getSourceResourceClass());
		Assert.assertEquals(Project.class, relRepo.getTargetResourceClass());
	}

	@Test
	public void testCreate() {
		Schedule schedule = new Schedule();
		schedule.setName("mySchedule");
		scheduleRepo.create(schedule);

		QuerySpec querySpec = new QuerySpec(Schedule.class);
		ResourceList<Schedule> list = scheduleRepo.findAll(querySpec);
		Assert.assertEquals(1, list.size());
		schedule = list.get(0);
		Assert.assertNotNull(schedule.getId());
	}

	@Test
	public void testNonPostableResource() {
		ReadOnlyTask readOnlyTask = new ReadOnlyTask();
		readOnlyTask.setId(12L);
		ResourceRepository<ReadOnlyTask, Object> repository = testContainer.getRepositoryForType(ReadOnlyTask.class);
		try {
			repository.create(readOnlyTask);
		}
		catch (MethodNotAllowedException e) {
			// ok
			Assert.assertNotNull(e.getUrl());
		}
	}

	@Test
	public void testOverlappingPaths() {
		HistoricTask task = new HistoricTask();
		task.setId(13L);
		task.setName("mySchedule");
		ResourceRepository<HistoricTask, Object> repository = testContainer.getRepositoryForType(HistoricTask.class);
		repository.create(task);
		ResourceList<HistoricTask> historicTasks = repository.findAll(new QuerySpec(HistoricTask.class));
		Assert.assertEquals(1, historicTasks.size());
		ResourceList<Task> tasks = taskRepo.findAll(new QuerySpec(Task.class));
		Assert.assertEquals(0, tasks.size());
	}

	@Test
	public void testRenamedPath() {
		RelocatedTask task = new RelocatedTask();
		task.setId(13L);
		task.setName("mySchedule");
		ResourceRepository<RelocatedTask, Object> repository = testContainer.getRepositoryForType(RelocatedTask.class);
		repository.create(task);
		ResourceList<RelocatedTask> historicTasks = repository.findAll(new QuerySpec(RelocatedTask.class));
		Assert.assertEquals(1, historicTasks.size());

		RegistryEntry entry = testContainer.getBoot().getResourceRegistry().getEntry(RelocatedTask.class);
		Assert.assertEquals("taskNewPath", entry.getResourceInformation().getResourcePath());
	}

	@Test
	public void testNonPatchableResource() {
		ReadOnlyTask readOnlyTask = new ReadOnlyTask();
		readOnlyTask.setId(12L);
		ResourceRepository<ReadOnlyTask, Object> repository = testContainer.getRepositoryForType(ReadOnlyTask.class);
		try {
			repository.save(readOnlyTask);
		}
		catch (MethodNotAllowedException e) {
			// ok
			Assert.assertNotNull(e.getUrl());
		}
	}

	@Test
	public void testNonDeletableResource() {
		ResourceRepository<ReadOnlyTask, Object> repository = testContainer.getRepositoryForType(ReadOnlyTask.class);
		try {
			repository.delete(12L);
		}
		catch (MethodNotAllowedException e) {
			// ok
			Assert.assertNotNull(e.getUrl());
		}
	}

	@Test
	public void testCreateAnyFields() {
		Schedule schedule = new Schedule();
		schedule.setName("mySchedule2");
		schedule.setAnyFields("randomfield1", 1234);
		schedule.setAnyFields("randomfield2", "test");

		Schedule result = scheduleRepo.create(schedule);

		QuerySpec querySpec = new QuerySpec(Schedule.class);
		ResourceList<Schedule> list = scheduleRepo.findAll(querySpec);
		Assert.assertEquals(1, list.size());

		Assert.assertFalse(result.getAnyFields().isEmpty());
		Assert.assertEquals(1234, result.getAnyFields().get("randomfield1"));
		Assert.assertEquals("test", result.getAnyFields().get("randomfield2"));

	}


	@Test
	public void testJsonApiResponseContentTypeReceived() throws IOException {
		String url = testContainer.getBaseUrl() + "/schedules";
		OkHttpClient client = new OkHttpClient();
		Request request = new Request.Builder().url(url).build();
		Response response = client.newCall(request).execute();
		Assert.assertEquals(removeWhiteSpace(HttpHeaders.JSONAPI_CONTENT_TYPE_AND_CHARSET),
				removeWhiteSpace(response.header(HttpHeaders.HTTP_CONTENT_TYPE)));
	}

	@Test
	public void testInvalidMethod() throws IOException {
		String url = testContainer.getBaseUrl() + "/schedules";
		OkHttpClient client = new OkHttpClient();
		RequestBody body = RequestBody.create(MediaType.parse(HttpHeaders.JSONAPI_CONTENT_TYPE_AND_CHARSET), new byte[1]);
		Request request = new Request.Builder().url(url).put(body).build();
		Response response = client.newCall(request).execute();
		Assert.assertEquals(io.crnk.core.engine.http.HttpStatus.METHOD_NOT_ALLOWED_405, response.code());
	}


	private String removeWhiteSpace(String value) {
		return value.replace(" ", "");
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
	public void testResourceTypeHolderCarriesResourceType() {
		Task task = new Task();
		task.setId(1L);
		task.setName("task");
		taskRepo.create(task);

		TaskSubType subType = new TaskSubType();
		subType.setId(13L);
		subType.setName("task");
		taskRepo.create(subType);

		ResourceList<Task> tasks = taskRepo.findAll(new QuerySpec(Task.class));
		for (Task fetchedTask : tasks) {
			if (fetchedTask.getId() == 13L) {
				Assert.assertEquals("tasksSubType", fetchedTask.getType());
			}
			else {
				Assert.assertEquals("tasks", fetchedTask.getType());
			}
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
	public void testSelfLinks() {
		for (int i = 0; i < 5; i++) {
			Task task = new Task();
			task.setId(Long.valueOf(i));
			task.setName("task" + i);
			taskRepo.create(task);
		}
		QuerySpec querySpec = new QuerySpec(Task.class);
		querySpec.addSort(new SortSpec(Arrays.asList("name"), Direction.DESC));
		ResourceList<Task> tasks = taskRepo.findAll(querySpec);

		DefaultPagedLinksInformation links = tasks.getLinks(DefaultPagedLinksInformation.class);
		Assert.assertNotNull(links.getSelf());
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
	public void testFindByMap() {
		for (int i = 0; i < 10; i++) {
			Map<String, String> map = new HashMap<>();
			map.put("a", "b" + i);

			Schedule project = new Schedule();
			project.setId((long) i);
			project.setName("test" + i);
			project.setCustomData(map);
			scheduleRepo.create(project);
		}

		QuerySpec querySpec = new QuerySpec(Schedule.class);
		querySpec.addFilter(new FilterSpec(Arrays.asList("customData", "a"), FilterOperator.EQ, "b1"));
		List<Schedule> matches = scheduleRepo.findAll(querySpec);

		Assert.assertEquals(1, matches.size());
		Schedule match = matches.get(0);
		Assert.assertEquals(1L, match.getId().longValue());
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

		// check relationship available => ID is serialized, so a proxy is available
		Task savedTask = taskRepo.findOne(task.getId(), new QuerySpec(Task.class));
		Assert.assertNotNull(savedTask.getSchedule());

		// check available from opposite side
		QuerySpec querySpec = new QuerySpec(Schedule.class);
		querySpec.includeRelation(PathSpec.of("tasks"));
		Schedule savedSchedule = scheduleRepo.findOne(schedule.getId(), querySpec);
		Assert.assertEquals(1, savedSchedule.getTasks().size());
		Task relatedTask = savedSchedule.getTasks().iterator().next();
		Assert.assertEquals(task.getId(), relatedTask.getId());
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
		Project project = new Project();
		project.setId(2L);
		project.setName("project");
		project = projectRepo.create(project);

		Task task = new Task();
		task.setId(1L);
		task.setName("task");
		task.setProject(project);
		taskRepo.create(task);

		// since lazy, will not be sent to client if not requested
		QuerySpec querySpec = new QuerySpec(Task.class);
		Task svaedTask = taskRepo.findOne(task.getId(), querySpec);
		Assert.assertNull(svaedTask.getProject());

		querySpec.includeRelation(Arrays.asList("project"));
		svaedTask = taskRepo.findOne(task.getId(), querySpec);
		Assert.assertNotNull(svaedTask.getProject());

		// null
		svaedTask.setProject(project);
		taskRepo.save(svaedTask);

		// still not null because cannot differantiate between not loaded and
		// nulled
		Task updatedSchedule = taskRepo.findOne(task.getId(), querySpec);
		Assert.assertNotNull(updatedSchedule.getProject());
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
		ResourceList<Project> relProjects = relRepo.findManyTargets(task.getId(), "projects", new QuerySpec(Project.class));
		Assert.assertEquals(2, relProjects.size());
		relRepo.setRelations(task, Arrays.asList(project1.getId()), "projects");

		relProjects = relRepo.findManyTargets(task.getId(), "projects", new QuerySpec(Project.class));
		Assert.assertEquals(1, relProjects.size());
		Assert.assertEquals(project1.getId(), relProjects.get(0).getId());
		ProjectRepository.ProjectsLinksInformation projectLinks = relProjects.getLinks(ProjectRepository.ProjectsLinksInformation.class);
		ProjectRepository.ProjectsMetaInformation projecsMeta = relProjects.getMeta(ProjectRepository.ProjectsMetaInformation.class);
		Assert.assertEquals("linkValue", projectLinks.getLinkValue().getHref());
		Assert.assertEquals("metaValue", projecsMeta.getMetaValue());
		// TODO HTTP DELETE method with payload not supported? at least in
		// Jersey
		/*
		relRepo.removeRelations(task, Arrays.asList(project1.getId()),
				"projects");
		relProjects = relRepo.findManyRelations(task.getId(), "projects", new QuerySpec(Task.class));
		Assert.assertEquals(0, relProjects.size());
		*/
	}


	@Test
	public void testRelatedInformation() {
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

		relRepo.setRelations(task, Arrays.asList(project0.getId(), project1.getId()), "projects");

		QuerySpec querySpec = new QuerySpec(Task.class);
		querySpec.includeRelation(PathSpec.of("projects"));
		Task queriedTask = taskRepo.findOne(task.getId(), querySpec);
		ResourceList<Project> projects = queriedTask.getProjects();
		Assert.assertEquals(2, projects.size());
		ProjectRepository.ProjectsLinksInformation relationLinks = projects.getLinks(ProjectRepository.ProjectsLinksInformation.class);
		Assert.assertEquals("linkValue", relationLinks.getLinkValue().getHref());
		Assert.assertTrue(relationLinks.getSelf().getHref().endsWith("/tasks/3/relationships/projects"));
		Assert.assertTrue(relationLinks.getRelated().getHref().endsWith("/tasks/3/projects"));
		ProjectRepository.ProjectsMetaInformation relationMeta = projects.getMeta(ProjectRepository.ProjectsMetaInformation.class);
		Assert.assertEquals("metaValue", relationMeta.getMetaValue());
	}


	@Test
	public void testRenaming() {
		for (int i = 0; i < 10; i++) {
			Schedule schedule = new Schedule();
			schedule.setId((long) i);
			schedule.setName("schedule" + i);
			schedule.setDesc("description" + i);
			scheduleRepo.create(schedule);
		}

		QuerySpec querySpec = new QuerySpec(Schedule.class);
		querySpec.addSort(new SortSpec(Arrays.asList("desc"), Direction.DESC));
		querySpec.includeField(Arrays.asList("desc"));
		querySpec.addFilter(new FilterSpec(Arrays.asList("desc"), FilterOperator.EQ,
				Arrays.asList("description0", "description1", "description2")));

		List<Schedule> schedules = scheduleRepo.findAll(querySpec);
		Assert.assertEquals(3, schedules.size());

		for (int i = 0; i < schedules.size(); i++) {
			Schedule schedule = schedules.get(schedules.size() - 1 - i);
			Assert.assertEquals("description" + i, schedule.getDesc());
			Assert.assertNull(schedule.getName());
		}

	}

	@Test
	public void includeFieldsWihtoutInclusion() {
		Project project = new Project();
		project.setId(1L);
		project.setName("project");
		projectRepo.create(project);

		Task task = new Task();
		task.setId(3L);
		task.setName("test");
		task.setProject(project);
		taskRepo.create(task);

		QuerySpec querySpec = new QuerySpec(Task.class);
		querySpec.includeField(PathSpec.of("id"));
		querySpec.includeField(PathSpec.of("name"));
		Task queriedTask = taskRepo.findOne(task.getId(), querySpec);
		Assert.assertNull(queriedTask.getProject());
		Assert.assertNotNull(queriedTask.getName());
		Assert.assertNotNull(queriedTask.getId());
	}

	@Test
	public void includeFieldsWithInclusion() {
		Project project = new Project();
		project.setId(1L);
		project.setName("project");
		projectRepo.create(project);

		Task task = new Task();
		task.setId(3L);
		task.setName("test");
		task.setProject(project);
		taskRepo.create(task);

		QuerySpec querySpec = new QuerySpec(Task.class);
		querySpec.includeField(PathSpec.of("id"));
		querySpec.includeRelation(PathSpec.of("project"));
		Task queriedTask = taskRepo.findOne(task.getId(), querySpec);
		Assert.assertNotNull(queriedTask.getProject());
		Assert.assertEquals("project", queriedTask.getProject().getName());
		Assert.assertNull(queriedTask.getName());
		Assert.assertNotNull(queriedTask.getId());
	}

}
