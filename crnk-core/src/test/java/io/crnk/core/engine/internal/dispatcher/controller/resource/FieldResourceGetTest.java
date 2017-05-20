package io.crnk.core.engine.internal.dispatcher.controller.resource;

import io.crnk.core.engine.dispatcher.Response;
import io.crnk.core.engine.document.Relationship;
import io.crnk.core.engine.document.Resource;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.dispatcher.controller.BaseControllerTest;
import io.crnk.core.engine.internal.dispatcher.controller.FieldResourceGet;
import io.crnk.core.engine.internal.dispatcher.path.JsonPath;
import io.crnk.core.engine.internal.dispatcher.path.ResourcePath;
import io.crnk.core.engine.internal.repository.RelationshipRepositoryAdapter;
import io.crnk.core.engine.internal.repository.ResourceRepositoryAdapter;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.mock.models.Project;
import io.crnk.core.mock.models.Task;
import io.crnk.core.mock.models.User;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.queryspec.internal.QuerySpecAdapter;
import io.crnk.legacy.internal.QueryParamsAdapter;
import io.crnk.legacy.queryParams.QueryParams;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class FieldResourceGetTest extends BaseControllerTest {
	private static final String REQUEST_TYPE = "GET";

	@Test
	public void onValidRequestShouldAcceptIt() {
		// GIVEN
		JsonPath jsonPath = pathBuilder.buildPath("tasks/1/project");
		ResourceRegistry resourceRegistry = mock(ResourceRegistry.class);
		FieldResourceGet sut = new FieldResourceGet(resourceRegistry, objectMapper, typeParser, documentMapper);

		// WHEN
		boolean result = sut.isAcceptable(jsonPath, REQUEST_TYPE);

		// THEN
		assertThat(result).isTrue();
	}

	@Test
	public void onRelationshipRequestShouldDenyIt() {
		// GIVEN
		JsonPath jsonPath = new ResourcePath("tasks/1/relationships/project");
		ResourceRegistry resourceRegistry = mock(ResourceRegistry.class);
		FieldResourceGet sut = new FieldResourceGet(resourceRegistry, objectMapper, typeParser, documentMapper);

		// WHEN
		boolean result = sut.isAcceptable(jsonPath, REQUEST_TYPE);

		// THEN
		assertThat(result).isFalse();
	}

	@Test
	public void onNonRelationRequestShouldDenyIt() {
		// GIVEN
		JsonPath jsonPath = new ResourcePath("tasks");
		ResourceRegistry resourceRegistry = mock(ResourceRegistry.class);
		FieldResourceGet sut = new FieldResourceGet(resourceRegistry, objectMapper, typeParser, documentMapper);

		// WHEN
		boolean result = sut.isAcceptable(jsonPath, REQUEST_TYPE);

		// THEN
		assertThat(result).isFalse();
	}

	@Test
	public void onGivenRequestFieldResourceGetShouldHandleIt() throws Exception {
		// GIVEN

		JsonPath jsonPath = pathBuilder.buildPath("/tasks/1/project");
		FieldResourceGet sut = new FieldResourceGet(resourceRegistry, objectMapper, typeParser, documentMapper);

		// WHEN
		Response response = sut.handle(jsonPath, new QueryParamsAdapter(new QueryParams()), null, null);

		// THEN
		Assert.assertNotNull(response);
	}

	@Test
	public void onGivenRequestFieldResourcesGetShouldHandleIt() throws Exception {
		// GIVEN

		JsonPath jsonPath = pathBuilder.buildPath("/users/1/assignedProjects");
		FieldResourceGet sut = new FieldResourceGet(resourceRegistry, objectMapper, typeParser, documentMapper);

		// WHEN
		Response response = sut.handle(jsonPath, new QuerySpecAdapter(new QuerySpec(Project.class), resourceRegistry), null, null);

		// THEN
		Assert.assertNotNull(response);
	}

	@Test
	@SuppressWarnings({"rawtypes", "unchecked"})
	public void onGivenIncludeRequestFieldResourcesGetShouldHandleIt() throws Exception {

		// get repositories
		ResourceRepositoryAdapter userRepo = resourceRegistry.findEntry(User.class).getResourceRepository(null);
		ResourceRepositoryAdapter projectRepo = resourceRegistry.findEntry(Project.class).getResourceRepository(null);
		ResourceRepositoryAdapter taskRepo = resourceRegistry.findEntry(Task.class).getResourceRepository(null);

		RelationshipRepositoryAdapter relRepositoryUserToProject = resourceRegistry.findEntry(User.class).getRelationshipRepositoryForClass(Project.class, null);
		RelationshipRepositoryAdapter relRepositoryProjectToTask = resourceRegistry.findEntry(Project.class).getRelationshipRepositoryForClass(Task.class, null);

		ResourceInformation userInfo = resourceRegistry.findEntry(User.class).getResourceInformation();
		ResourceInformation projectInfo = resourceRegistry.findEntry(Project.class).getResourceInformation();
		ResourceField includedTaskField = projectInfo.findRelationshipFieldByName("includedTask");
		ResourceField assignedProjectsField = userInfo.findRelationshipFieldByName("assignedProjects");

		// setup test data
		User user = new User();
		user.setId(1L);
		userRepo.create(user, null);
		Project project = new Project();
		project.setId(2L);
		projectRepo.create(project, null);
		Task task = new Task();
		task.setId(3L);
		taskRepo.create(task, null);
		relRepositoryUserToProject.setRelations(user, Collections.singletonList(project.getId()), assignedProjectsField, null);
		relRepositoryProjectToTask.setRelation(project, task.getId(), includedTaskField, null);

		Map<String, Set<String>> params = new HashMap<String, Set<String>>();
		addParams(params, "include[projects]", "includedTask");
		QueryParams queryParams = queryParamsBuilder.buildQueryParams(params);
		QueryAdapter queryAdapter = new QueryParamsAdapter(projectInfo, queryParams, moduleRegistry);
		JsonPath jsonPath = pathBuilder.buildPath("/users/1/assignedProjects");
		FieldResourceGet sut = new FieldResourceGet(resourceRegistry, objectMapper, typeParser, documentMapper);

		Response response = sut.handle(jsonPath, queryAdapter, null, null);

		// THEN
		Assert.assertNotNull(response);
		Assert.assertNotNull(response.getDocument().getData());
		List<Resource> entityList = ((List<Resource>) response.getDocument().getData().get());
		Assert.assertEquals(true, entityList.size() > 0);
		Assert.assertEquals("projects", entityList.get(0).getType());
		Resource returnedProject = entityList.get(0);
		Assert.assertEquals(project.getId().toString(), returnedProject.getId());
		Relationship relationship = returnedProject.getRelationships().get("includedTask");
		Assert.assertNotNull(relationship);
		Assert.assertEquals(task.getId().toString(), relationship.getSingleData().get().getId());
	}

}
