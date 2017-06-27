package io.crnk.core.engine.internal.dispatcher.controller.resource;

import io.crnk.core.engine.dispatcher.Response;
import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.document.Resource;
import io.crnk.core.engine.document.ResourceIdentifier;
import io.crnk.core.engine.internal.dispatcher.controller.BaseControllerTest;
import io.crnk.core.engine.internal.dispatcher.controller.RelationshipsResourcePost;
import io.crnk.core.engine.internal.dispatcher.controller.ResourceGet;
import io.crnk.core.engine.internal.dispatcher.controller.ResourcePost;
import io.crnk.core.engine.internal.dispatcher.path.JsonPath;
import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.core.mock.models.Project;
import io.crnk.core.mock.repository.TaskToProjectRepository;
import io.crnk.core.resource.RestrictedQueryParamsMembers;
import io.crnk.core.utils.Nullable;
import io.crnk.legacy.internal.QueryParamsAdapter;
import io.crnk.legacy.queryParams.DefaultQueryParamsParser;
import io.crnk.legacy.queryParams.QueryParams;
import io.crnk.legacy.queryParams.QueryParamsBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class ResourceGetTest extends BaseControllerTest {

	private static final String REQUEST_TYPE = "GET";

	@Before
	public void before() throws IOException {
		this.prepare();
	}

	@Test
	public void onGivenRequestCollectionGetShouldDenyIt() {
		// GIVEN
		JsonPath jsonPath = pathBuilder.build("/tasks/");
		ResourceGet sut = new ResourceGet(resourceRegistry, typeParser, documentMapper);

		// WHEN
		boolean result = sut.isAcceptable(jsonPath, REQUEST_TYPE);

		// THEN
		Assert.assertEquals(result, false);
	}

	@Test
	public void onGivenRequestResourceGetShouldAcceptIt() {
		// GIVEN
		JsonPath jsonPath = pathBuilder.build("/tasks/2");
		ResourceGet sut = new ResourceGet(resourceRegistry, typeParser, documentMapper);

		// WHEN
		boolean result = sut.isAcceptable(jsonPath, REQUEST_TYPE);

		// THEN
		Assert.assertEquals(result, true);
	}

	@Test
	public void onMethodMismatchShouldDenyIt() {
		// GIVEN
		JsonPath jsonPath = pathBuilder.build("/tasks/2");
		ResourceGet sut = new ResourceGet(resourceRegistry, typeParser, documentMapper);

		// WHEN
		boolean result = sut.isAcceptable(jsonPath, "POST");

		// THEN
		Assert.assertEquals(result, false);
	}

	@Test
	public void onGivenRequestResourceGetShouldHandleIt() throws Exception {
		// GIVEN
		Document newTaskBody = new Document();
		Resource data = createTask();
		newTaskBody.setData(Nullable.of((Object) data));

		JsonPath taskPath = pathBuilder.build("/tasks");

		// WHEN
		ResourcePost
				resourcePost = new ResourcePost(resourceRegistry, PROPERTIES_PROVIDER, typeParser, objectMapper, documentMapper);
		Response taskResponse = resourcePost.handle(taskPath, new QueryParamsAdapter(REQUEST_PARAMS), null, newTaskBody);
		assertThat(taskResponse.getDocument().getData().get()).isExactlyInstanceOf(Resource.class);
		String taskId = ((Resource) taskResponse.getDocument().getData().get()).getId();
		assertThat(taskId).isNotNull();

		// GIVEN
		JsonPath jsonPath = pathBuilder.build("/tasks/" + taskId);
		ResourceGet sut = new ResourceGet(resourceRegistry, typeParser, documentMapper);

		// WHEN
		Response response = sut.handle(jsonPath, new QueryParamsAdapter(REQUEST_PARAMS), null, null);

		// THEN
		Assert.assertNotNull(response);
	}

	@Test(expected = ResourceNotFoundException.class)
	public void onGivenRequestResourceGetShouldThrowError() throws Exception {
		// GIVEN
		JsonPath jsonPath = pathBuilder.build("/tasks/" + -1);
		ResourceGet sut = new ResourceGet(resourceRegistry, typeParser, documentMapper);

		// WHEN
		Response response = sut.handle(jsonPath, new QueryParamsAdapter(REQUEST_PARAMS), null, null);

		// THEN
		Assert.assertNull(response);
	}

	@Test
	public void onGivenRequestResourceShouldLoadAutoIncludeFields() throws Exception {
		// GIVEN
		JsonPath jsonPath = pathBuilder.build("/task-with-lookup/1");
		ResourceGet responseGetResp = new ResourceGet(resourceRegistry, typeParser, documentMapper);
		Map<String, Set<String>> queryParams = new HashMap<>();
		queryParams.put(RestrictedQueryParamsMembers.include.name() + "[task-with-lookup]", new HashSet<>(Arrays.asList("project", "projectNull", "projectOverridden", "projectOverriddenNull")));
		QueryParams queryParamsObject = new QueryParamsBuilder(new DefaultQueryParamsParser()).buildQueryParams(queryParams);

		// WHEN
		Response response = responseGetResp.handle(jsonPath, new QueryParamsAdapter(queryParamsObject), null, null);

		// THEN
		Assert.assertNotNull(response);
		assertThat(response.getDocument().getData().get()).isExactlyInstanceOf(Resource.class);
		assertThat(response.getDocument().getSingleData().get().getType()).isEqualTo("task-with-lookup");
		Resource responseData = response.getDocument().getSingleData().get();
		assertThat(responseData.getRelationships().get("project").getSingleData().get().getId()).isEqualTo("42");
		assertThat(responseData.getRelationships().get("projectNull").getSingleData().get().getId()).isEqualTo("1");
		assertThat(responseData.getRelationships().get("projectOverridden").getSingleData().get().getId()).isEqualTo("1");
		assertThat(responseData.getRelationships().get("projectOverriddenNull").getSingleData().get().getId()).isEqualTo("1");
	}

	@Test
	public void onGivenRequestResourceShouldNotLoadAutoIncludeFields() throws Exception {
		// GIVEN
		Document newTaskBody = new Document();
		Resource data = createTask();
		newTaskBody.setData(Nullable.of((Object) data));

		JsonPath taskPath = pathBuilder.build("/tasks");
		ResourcePost resourcePost = new ResourcePost(resourceRegistry, PROPERTIES_PROVIDER, typeParser, objectMapper, documentMapper);

		// WHEN -- adding a task
		Response taskResponse = resourcePost.handle(taskPath, new QueryParamsAdapter(REQUEST_PARAMS), null, newTaskBody);

		// THEN
		assertThat(taskResponse.getDocument().getSingleData().get()).isExactlyInstanceOf(Resource.class);
		assertThat(taskResponse.getDocument().getSingleData().get().getType()).isEqualTo("tasks");
		assertThat(taskResponse.getDocument().getSingleData().get().getId()).isNotNull();

		/* ------- */

		// GIVEN
		Document newProjectBody = new Document();
		newProjectBody.setData(Nullable.of((Object) createProject()));

		JsonPath projectPath = pathBuilder.build("/projects");

		// WHEN -- adding a project
		Response projectResponse = resourcePost.handle(projectPath, new QueryParamsAdapter(REQUEST_PARAMS), null, newProjectBody);

		// THEN
		assertThat(projectResponse.getDocument().getSingleData().get()).isExactlyInstanceOf(Resource.class);
		assertThat(projectResponse.getDocument().getSingleData().get().getType()).isEqualTo("projects");
		assertThat(projectResponse.getDocument().getSingleData().get().getId()).isNotNull();
		assertThat(projectResponse.getDocument().getSingleData().get().getAttributes().get("name").asText()).isEqualTo("sample project");

		/* ------- */

		// GIVEN
		Document newTaskToProjectBody = new Document();
		ResourceIdentifier reldata = new ResourceIdentifier();
		newTaskToProjectBody.setData(Nullable.of((Object) data));
		data.setType("projects");
		data.setId("2");

		JsonPath savedTaskPath = pathBuilder.build("/tasks/" + TASK_ID + "/relationships/project");
		RelationshipsResourcePost sut = new RelationshipsResourcePost(resourceRegistry, typeParser);

		// WHEN -- adding a relation between task and project
		Response projectRelationshipResponse = sut.handle(savedTaskPath, new QueryParamsAdapter(REQUEST_PARAMS), null, newTaskToProjectBody);
		assertThat(projectRelationshipResponse).isNotNull();

		// THEN
		TaskToProjectRepository taskToProjectRepository = new TaskToProjectRepository();
		Project project = taskToProjectRepository.findOneTarget(TASK_ID, "project", REQUEST_PARAMS);
		assertThat(project.getId()).isEqualTo(PROJECT_ID);

		// Given
		JsonPath jsonPath = pathBuilder.build("/tasks/" + TASK_ID);
		ResourceGet responseGetResp = new ResourceGet(resourceRegistry, typeParser, documentMapper);
		Map<String, Set<String>> queryParams = new HashMap<>();
		queryParams.put(RestrictedQueryParamsMembers.include.name() + "[tasks]", Collections.singleton("[\"project\"]"));
		QueryParams requestParams = new QueryParamsBuilder(new DefaultQueryParamsParser()).buildQueryParams(queryParams);

		// WHEN
		Response response = responseGetResp.handle(jsonPath, new QueryParamsAdapter(requestParams), null, null);

		// THEN
		Assert.assertNotNull(response);
		assertThat(response.getDocument().getSingleData().get().getType()).isEqualTo("tasks");
		assertThat(taskResponse.getDocument().getSingleData().get().getRelationships().get("project").getData().get()).isNull();
	}

}
