package io.crnk.core.engine.internal.dispatcher.controller.resource;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import io.crnk.core.mock.models.Project;
import io.crnk.core.mock.models.Task;
import io.crnk.core.mock.models.User;
import io.crnk.legacy.internal.QueryParamsAdapter;
import io.crnk.legacy.queryParams.QueryParams;
import org.junit.Assert;
import org.junit.Test;

public class FieldResourceGetTest extends BaseControllerTest {

	private static final String REQUEST_TYPE = "GET";

	@Test
	public void onValidRequestShouldAcceptIt() {
		// GIVEN
		JsonPath jsonPath = pathBuilder.build("tasks/1/project");
		FieldResourceGet sut = new FieldResourceGet();
		sut.init(controllerContext);

		// WHEN
		boolean result = sut.isAcceptable(jsonPath, REQUEST_TYPE);

		// THEN
		assertThat(result).isTrue();
	}

	@Test
	public void onRelationshipRequestShouldDenyIt() {
		// GIVEN
		JsonPath jsonPath = new ResourcePath("tasks/1/relationships/project");
		FieldResourceGet sut = new FieldResourceGet();
		sut.init(controllerContext);

		// WHEN
		boolean result = sut.isAcceptable(jsonPath, REQUEST_TYPE);

		// THEN
		assertThat(result).isFalse();
	}

	@Test
	public void onNonRelationRequestShouldDenyIt() {
		// GIVEN
		JsonPath jsonPath = new ResourcePath("tasks");
		FieldResourceGet sut = new FieldResourceGet();
		sut.init(controllerContext);

		// WHEN
		boolean result = sut.isAcceptable(jsonPath, REQUEST_TYPE);

		// THEN
		assertThat(result).isFalse();
	}

	@Test
	public void onGivenRequestFieldResourceGetShouldHandleIt() throws Exception {
		// GIVEN

		JsonPath jsonPath = pathBuilder.build("/tasks/1/project");
		FieldResourceGet sut = new FieldResourceGet();
		sut.init(controllerContext);

		// WHEN
		Response response = sut.handle(jsonPath, emptyProjectQuery, null, null);

		// THEN
		Assert.assertNotNull(response);
	}

	@Test
	public void onGivenRequestFieldResourcesGetShouldHandleIt() throws Exception {
		// GIVEN

		JsonPath jsonPath = pathBuilder.build("/users/1/assignedProjects");
		FieldResourceGet sut = new FieldResourceGet();
		sut.init(controllerContext);

		// WHEN
		Response response = sut.handle(jsonPath, emptyProjectQuery, null, null);

		// THEN
		Assert.assertNotNull(response);
	}

	@Test
	@SuppressWarnings({"rawtypes", "unchecked"})
	public void onGivenIncludeRequestFieldResourcesGetShouldHandleIt() throws Exception {

		// get repositories
		ResourceRepositoryAdapter userRepo = resourceRegistry.getEntry(User.class).getResourceRepository(null);
		ResourceRepositoryAdapter projectRepo = resourceRegistry.getEntry(Project.class).getResourceRepository(null);
		ResourceRepositoryAdapter taskRepo = resourceRegistry.getEntry(Task.class).getResourceRepository(null);

		RelationshipRepositoryAdapter relRepositoryUserToProject =
				resourceRegistry.getEntry(User.class).getRelationshipRepository("assignedProjects", null);
		RelationshipRepositoryAdapter relRepositoryProjectToTask =
				resourceRegistry.getEntry(Project.class).getRelationshipRepository("tasks", null);

		ResourceInformation userInfo = resourceRegistry.getEntry(User.class).getResourceInformation();
		ResourceInformation projectInfo = resourceRegistry.getEntry(Project.class).getResourceInformation();
		ResourceField includedTaskField = projectInfo.findRelationshipFieldByName("includedTask");
		ResourceField assignedProjectsField = userInfo.findRelationshipFieldByName("assignedProjects");

		// setup test data
		User user = new User();
		user.setId(1L);
		userRepo.create(user, emptyUserQuery);
		Project project = new Project();
		project.setId(2L);
		projectRepo.create(project, emptyProjectQuery);
		Task task = new Task();
		task.setId(3L);
		taskRepo.create(task, emptyTaskQuery);
		relRepositoryUserToProject
				.setRelations(user, Collections.singletonList(project.getId()), assignedProjectsField, emptyProjectQuery);
		relRepositoryProjectToTask.setRelation(project, task.getId(), includedTaskField, emptyTaskQuery);

		Map<String, Set<String>> params = new HashMap<String, Set<String>>();
		addParams(params, "include[projects]", "includedTask");
		QueryParams queryParams = queryParamsBuilder.buildQueryParams(params);
		QueryAdapter queryAdapter = container.toQueryAdapter(Project.class, queryParams);
		JsonPath jsonPath = pathBuilder.build("/users/1/assignedProjects");
		FieldResourceGet sut = new FieldResourceGet();
		sut.init(controllerContext);

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
