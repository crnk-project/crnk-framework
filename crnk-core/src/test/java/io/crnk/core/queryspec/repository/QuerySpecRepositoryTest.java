package io.crnk.core.queryspec.repository;

import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.internal.repository.RelationshipRepositoryAdapter;
import io.crnk.core.engine.internal.repository.ResourceRepositoryAdapter;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.mock.models.Project;
import io.crnk.core.mock.models.Schedule;
import io.crnk.core.mock.models.Task;
import io.crnk.core.mock.repository.ScheduleRepository;
import io.crnk.core.mock.repository.ScheduleRepositoryImpl;
import io.crnk.core.queryspec.AbstractQuerySpecTest;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.queryspec.internal.QuerySpecAdapter;
import io.crnk.core.repository.response.JsonApiResponse;
import io.crnk.legacy.internal.QueryParamsAdapter;
import io.crnk.legacy.queryParams.QueryParams;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.Serializable;
import java.util.*;

public class QuerySpecRepositoryTest extends AbstractQuerySpecTest {

	private ResourceRepositoryAdapter<Task, Long> taskAdapter;

	@SuppressWarnings("rawtypes")
	private RelationshipRepositoryAdapter projectRelAdapter;

	private ResourceRepositoryAdapter<Project, Serializable> projectAdapter;

	private RelationshipRepositoryAdapter<Project, Long, Task, Long> tasksRelAdapter;

	private ResourceRepositoryAdapter<Schedule, Serializable> scheduleAdapter;

	@Before
	public void setup() {
		TaskQuerySpecRepository.clear();
		ProjectQuerySpecRepository.clear();
		ScheduleRepositoryImpl.clear();

		super.setup();
		RegistryEntry taskEntry = resourceRegistry.getEntry(Task.class);
		RegistryEntry projectEntry = resourceRegistry.getEntry(Project.class);
		RegistryEntry scheduleEntry = resourceRegistry.getEntry(Schedule.class);
		TaskQuerySpecRepository repo = (TaskQuerySpecRepository) taskEntry.getResourceRepository(null).getResourceRepository();

		repo = Mockito.spy(repo);

		scheduleAdapter = scheduleEntry.getResourceRepository(null);
		projectAdapter = projectEntry.getResourceRepository(null);
		taskAdapter = taskEntry.getResourceRepository(null);
		projectRelAdapter = taskEntry.getRelationshipRepositoryForType("projects", null);
		tasksRelAdapter = projectEntry.getRelationshipRepositoryForType("tasks", null);
	}

	@Test
	public void testCrudWithQueryParamsInput() {
		Map<String, Set<String>> params = new HashMap<String, Set<String>>();
		addParams(params, "sort[tasks][name]", "asc");
		QueryParams queryParams = queryParamsBuilder.buildQueryParams(params);

		QueryParamsAdapter queryAdapter = new QueryParamsAdapter(queryParams);
		checkCrud(queryAdapter);
	}

	@Test
	public void findAllWithResourceListResult() {
		QuerySpec querySpec = new QuerySpec(Schedule.class);
		QueryAdapter adapter = new QuerySpecAdapter(querySpec, resourceRegistry);
		JsonApiResponse response = scheduleAdapter.findAll(adapter);
		Assert.assertTrue(response.getEntity() instanceof ScheduleRepository.ScheduleList);
		Assert.assertTrue(response.getLinksInformation() instanceof ScheduleRepository.ScheduleListLinks);
		Assert.assertTrue(response.getMetaInformation() instanceof ScheduleRepository.ScheduleListMeta);
	}

	@Test
	public void testCrudWithQuerySpec() {
		QuerySpec querySpec = new QuerySpec(Task.class);
		QueryAdapter adapter = new QuerySpecAdapter(querySpec, resourceRegistry);
		checkCrud(adapter);
	}

	@SuppressWarnings({"unchecked"})
	private void checkCrud(QueryAdapter queryAdapter) {
		// setup data
		Project project = new Project();
		project.setId(3L);
		project.setName("myProject");
		projectAdapter.create(project, queryAdapter);

		Task task = new Task();
		task.setId(2L);
		task.setName("myTask");
		task.setProject(project);
		task.setProjects(Arrays.asList(project));
		taskAdapter.create(task, queryAdapter);

		// adapter
		List<Task> tasks = (List<Task>) taskAdapter.findAll(queryAdapter).getEntity();
		Assert.assertEquals(1, tasks.size());
		Assert.assertEquals(task, taskAdapter.findOne(2L, queryAdapter).getEntity());
		tasks = (List<Task>) taskAdapter.findAll(Arrays.asList(2L), queryAdapter).getEntity();
		Assert.assertEquals(1, tasks.size());

		// relation adapter
		ResourceField projectField = resourceRegistry.getEntry(Task.class).getResourceInformation().findRelationshipFieldByName("project");
		ResourceField tasksField = resourceRegistry.getEntry(Project.class).getResourceInformation().findRelationshipFieldByName("tasks");
		projectRelAdapter.setRelation(task, project.getId(), projectField, queryAdapter);
		Assert.assertNotNull(task.getProject());
		Assert.assertEquals(1, project.getTasks().size());
		JsonApiResponse response = projectRelAdapter.findOneTarget(2L, projectField, queryAdapter);
		Assert.assertEquals(project.getId(), ((Project) response.getEntity()).getId());

		projectRelAdapter.setRelation(task, null, projectField, queryAdapter);
		response = projectRelAdapter.findOneTarget(2L, projectField, queryAdapter);
		Assert.assertNull(task.getProject());

		// warning: bidirectionality not properly implemented here, would
		// require changes to the model used in many other places
		task.setProject(null);
		project.getTasks().clear();

		tasksRelAdapter.addRelations(project, Arrays.asList(task.getId()), tasksField, queryAdapter);
		Assert.assertEquals(project, task.getProject());
		Assert.assertEquals(1, project.getTasks().size());
		List<Project> projects = (List<Project>) tasksRelAdapter.findManyTargets(3L, tasksField, queryAdapter).getEntity();
		Assert.assertEquals(1, projects.size());

		tasksRelAdapter.removeRelations(project, Arrays.asList(task.getId()), tasksField, queryAdapter);
		Assert.assertEquals(0, project.getTasks().size());
		task.setProject(null); // fix bidirectionality

		projects = (List<Project>) tasksRelAdapter.findManyTargets(3L, tasksField, queryAdapter).getEntity();
		Assert.assertEquals(0, projects.size());

		tasksRelAdapter.setRelations(project, Arrays.asList(task.getId()), tasksField, queryAdapter);
		Assert.assertEquals(project, task.getProject());
		Assert.assertEquals(1, project.getTasks().size());
		projects = (List<Project>) tasksRelAdapter.findManyTargets(3L, tasksField, queryAdapter).getEntity();
		Assert.assertEquals(1, projects.size());

		// check bulk find
		Map<?, JsonApiResponse> bulkMap = tasksRelAdapter.findBulkManyTargets(Arrays.asList(3L), tasksField, queryAdapter);
		Assert.assertEquals(1, bulkMap.size());
		Assert.assertTrue(bulkMap.containsKey(3L));
		projects = (List<Project>) bulkMap.get(3L).getEntity();
		Assert.assertEquals(1, projects.size());

		bulkMap = projectRelAdapter.findBulkOneTargets(Arrays.asList(2L), projectField, queryAdapter);
		Assert.assertEquals(1, bulkMap.size());
		Assert.assertTrue(bulkMap.containsKey(2L));
		Assert.assertNotNull(bulkMap.get(2L));

		// deletion
		taskAdapter.delete(task.getId(), queryAdapter);
		tasks = (List<Task>) taskAdapter.findAll(queryAdapter).getEntity();
		Assert.assertEquals(0, tasks.size());
		tasks = (List<Task>) taskAdapter.findAll(Arrays.asList(2L), queryAdapter).getEntity();
		Assert.assertEquals(0, tasks.size());

	}

}
