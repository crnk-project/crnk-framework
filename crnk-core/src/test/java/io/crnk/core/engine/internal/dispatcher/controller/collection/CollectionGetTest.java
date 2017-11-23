package io.crnk.core.engine.internal.dispatcher.controller.collection;

import io.crnk.core.engine.dispatcher.Response;
import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.document.Relationship;
import io.crnk.core.engine.document.Resource;
import io.crnk.core.engine.filter.ResourceModificationFilter;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.internal.dispatcher.controller.*;
import io.crnk.core.engine.internal.dispatcher.path.JsonPath;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.mock.models.Project;
import io.crnk.core.mock.models.Task;
import io.crnk.core.mock.repository.TaskToProjectRepository;
import io.crnk.core.resource.RestrictedQueryParamsMembers;
import io.crnk.core.resource.annotations.SerializeType;
import io.crnk.core.utils.Nullable;
import io.crnk.legacy.internal.QueryParamsAdapter;
import io.crnk.legacy.queryParams.DefaultQueryParamsParser;
import io.crnk.legacy.queryParams.QueryParams;
import io.crnk.legacy.queryParams.QueryParamsBuilder;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

public class CollectionGetTest extends BaseControllerTest {

	private static final String REQUEST_TYPE = "GET";


	@Test
	public void onGivenRequestCollectionGetShouldAcceptIt() {
		// GIVEN
		JsonPath jsonPath = pathBuilder.build("/tasks/");
		CollectionGet sut = new CollectionGet(resourceRegistry, typeParser, documentMapper);

		// WHEN
		boolean result = sut.isAcceptable(jsonPath, REQUEST_TYPE);

		// THEN
		Assert.assertEquals(result, true);
	}

	@Test
	public void onGivenRequestCollectionGetShouldDenyIt() {
		// GIVEN
		JsonPath jsonPath = pathBuilder.build("/tasks/2");
		CollectionGet sut = new CollectionGet(resourceRegistry, typeParser, documentMapper);

		// WHEN
		boolean result = sut.isAcceptable(jsonPath, REQUEST_TYPE);

		// THEN
		Assert.assertEquals(result, false);
	}

	@Test
	public void onGivenRequestCollectionGetShouldHandleIt() {
		// GIVEN

		JsonPath jsonPath = pathBuilder.build("/tasks/");
		CollectionGet sut = new CollectionGet(resourceRegistry, typeParser, documentMapper);

		// WHEN
		Response response = sut.handle(jsonPath, emptyTaskQuery, null, null);

		// THEN
		Assert.assertNotNull(response);
	}

	@Test
	public void onGivenRequestCollectionWithIdsGetShouldHandleIt() {
		// GIVEN

		JsonPath jsonPath = pathBuilder.build("/tasks/1,2");
		CollectionGet sut = new CollectionGet(resourceRegistry, typeParser, documentMapper);

		// WHEN
		Response response = sut.handle(jsonPath, emptyTaskQuery, null, null);

		// THEN
		Assert.assertNotNull(response);
	}

	@Test
	public void onGivenRequestResourceWithIdShouldSetIt() throws Exception {
		// GIVEN
		Document Document = new Document();
		Resource data = new Resource();
		Document.setData(Nullable.of((Object) data));
		long taskId = Long.MAX_VALUE - 1L;
		data.setType("tasks");
		data.setId(Long.toString(taskId));

		JsonPath taskPath = pathBuilder.build("/tasks");
		ResourcePost resourcePost = new ResourcePost(resourceRegistry, PROPERTIES_PROVIDER, typeParser, objectMapper, documentMapper, new ArrayList<ResourceModificationFilter>());

		// WHEN -- adding a task
		Response taskResponse = resourcePost.handle(taskPath, emptyTaskQuery, null, Document);

		// THEN
		assertThat(taskResponse.getDocument().getSingleData().get().getType()).isEqualTo("tasks");
		Long persistedTaskId = Long.parseLong(taskResponse.getDocument().getSingleData().get().getId());
		assertThat(persistedTaskId).isEqualTo(taskId);
	}

	@Test
	public void onGivenRequestResourceShouldLoadAutoIncludeFields() throws Exception {
		// GIVEN
		Document newTaskBody = new Document();
		Resource data = createTask();
		newTaskBody.setData(Nullable.of((Object) data));
		JsonPath taskPath = pathBuilder.build("/tasks");
		ResourcePost resourcePost = new ResourcePost(resourceRegistry, PROPERTIES_PROVIDER, typeParser, objectMapper, documentMapper, new ArrayList<ResourceModificationFilter>());

		// WHEN -- adding a task
		Response taskResponse = resourcePost.handle(taskPath, emptyTaskQuery, null, newTaskBody);

		// THEN
		assertThat(taskResponse.getDocument().getSingleData().get().getType()).isEqualTo("tasks");
		Long taskId = Long.parseLong(taskResponse.getDocument().getSingleData().get().getId());
		assertThat(taskId).isNotNull();

		/* ------- */

		// GIVEN
		Document newProjectBody = new Document();
		data = createProject();
		newProjectBody.setData(Nullable.of((Object) data));
		JsonPath projectPath = pathBuilder.build("/projects");

		// WHEN -- adding a project
		Response projectResponse = resourcePost.handle(projectPath, emptyProjectQuery, null, newProjectBody);

		// THEN
		assertThat(projectResponse.getDocument().getSingleData().get().getType()).isEqualTo("projects");
		assertThat(projectResponse.getDocument().getSingleData().get().getId()).isNotNull();
		assertThat(projectResponse.getDocument().getSingleData().get().getAttributes().get("name").asText()).isEqualTo("sample project");
		Long projectId = Long.parseLong(projectResponse.getDocument().getSingleData().get().getId());
		assertThat(projectId).isNotNull();

		/* ------- */

		// GIVEN
		Document newTaskToProjectBody = new Document();
		data = new Resource();
		newTaskToProjectBody.setData(Nullable.of((Object) Collections.singletonList(data)));
		data.setType("projects");
		data.setId(projectId.toString());

		JsonPath savedTaskPath = pathBuilder.build("/tasks/" + taskId + "/relationships/includedProjects");
		RelationshipsResourcePost sut = new RelationshipsResourcePost(resourceRegistry, typeParser, new ArrayList<ResourceModificationFilter>());

		// WHEN -- adding a relation between task and project
		Response projectRelationshipResponse = sut.handle(savedTaskPath, emptyProjectQuery, null, newTaskToProjectBody);
		assertThat(projectRelationshipResponse).isNotNull();

		// THEN
		TaskToProjectRepository taskToProjectRepository = new TaskToProjectRepository();
		Project project = taskToProjectRepository.findOneTarget(taskId, "includedProjects", new QueryParams());
		assertThat(project.getId()).isEqualTo(projectId);

		// Given
		JsonPath jsonPath = pathBuilder.build("/tasks/" + taskId);
		ResourceGet responseGetResp = new ResourceGet(resourceRegistry, typeParser, documentMapper);
		Map<String, Set<String>> queryParams = new HashMap<>();
		queryParams.put(RestrictedQueryParamsMembers.include.name() + "[tasks]", Collections.singleton("includedProjects"));
		QueryParams queryParams1 = new QueryParamsBuilder(new DefaultQueryParamsParser()).buildQueryParams(queryParams);

		// WHEN
		Response response = responseGetResp.handle(jsonPath, new QueryParamsAdapter(resourceRegistry.getEntry(Task.class).getResourceInformation(), queryParams1, moduleRegistry), null, null);

		// THEN
		Assert.assertNotNull(response);
		data = response.getDocument().getSingleData().get();
		assertThat(data.getType()).isEqualTo("tasks");
		Relationship relationship = data.getRelationships().get("includedProjects");
		assertThat(relationship.getCollectionData()).isNotNull();
		assertThat(relationship.getCollectionData().get().size()).isEqualTo(1);
		assertThat(relationship.getCollectionData().get().get(0).getId()).isEqualTo(projectId.toString());
	}

	@Test
	public void onGivenRequestResourceShouldNotLoadAutoIncludeFields() throws Exception {
		// GIVEN
		Document newTaskBody = new Document();
		Resource data = createTask();
		newTaskBody.setData(Nullable.of((Object) data));
		data.setType("tasks");

		JsonPath taskPath = pathBuilder.build("/tasks");
		ResourcePost resourcePost = new ResourcePost(resourceRegistry, PROPERTIES_PROVIDER, typeParser, objectMapper, documentMapper, new ArrayList<ResourceModificationFilter>());

		// WHEN -- adding a task
		Response taskResponse = resourcePost.handle(taskPath, emptyTaskQuery, null, newTaskBody);

		// THEN
		assertThat(taskResponse.getDocument().getSingleData().get().getType()).isEqualTo("tasks");
		Long taskId = Long.parseLong(taskResponse.getDocument().getSingleData().get().getId());
		assertThat(taskId).isNotNull();

		/* ------- */

		// GIVEN
		Document newProjectBody = new Document();
		data = createProject();
		newProjectBody.setData(Nullable.of((Object) data));

		JsonPath projectPath = pathBuilder.build("/projects");

		// WHEN -- adding a project
		Response projectResponse = resourcePost.handle(projectPath, emptyProjectQuery, null, newProjectBody);

		// THEN
		assertThat(projectResponse.getDocument().getSingleData().get().getType()).isEqualTo("projects");
		assertThat(projectResponse.getDocument().getSingleData().get().getId()).isNotNull();
		assertThat(projectResponse.getDocument().getSingleData().get().getAttributes().get("name").asText()).isEqualTo("sample project");
		Long projectId = Long.parseLong(projectResponse.getDocument().getSingleData().get().getId());
		assertThat(projectId).isNotNull();

		/* ------- */

		// GIVEN
		Document newTaskToProjectBody = new Document();
		data = new Resource();
		newTaskToProjectBody.setData(Nullable.of((Object) Collections.singletonList(data)));
		data.setType("projects");
		data.setId(projectId.toString());

		JsonPath savedTaskPath = pathBuilder.build("/tasks/" + taskId + "/relationships/projects");
		RelationshipsResourcePost sut = new RelationshipsResourcePost(resourceRegistry, typeParser, new ArrayList<ResourceModificationFilter>());

		// WHEN -- adding a relation between task and project
		Response projectRelationshipResponse = sut.handle(savedTaskPath, emptyProjectQuery, null, newTaskToProjectBody);
		assertThat(projectRelationshipResponse).isNotNull();

		// THEN
		TaskToProjectRepository taskToProjectRepository = new TaskToProjectRepository();
		Project project = taskToProjectRepository.findOneTarget(taskId, "projects", new QueryParams());
		assertThat(project.getId()).isNotNull();

		// Given
		JsonPath jsonPath = pathBuilder.build("/tasks/" + taskId);
		ResourceGet responseGetResp = new ResourceGet(resourceRegistry, typeParser, documentMapper);
		Map<String, Set<String>> queryParams = new HashMap<>();
		queryParams.put(RestrictedQueryParamsMembers.include.name() + "[tasks]", Collections.singleton("[\"projects\"]"));
		QueryParams requestParams = new QueryParamsBuilder(new DefaultQueryParamsParser()).buildQueryParams(queryParams);

		// WHEN
		Response response = responseGetResp.handle(jsonPath, new QueryParamsAdapter(resourceRegistry.getEntry(Task.class).getResourceInformation(), requestParams, moduleRegistry), null, null);

		// THEN
		Assert.assertNotNull(response);
		assertThat(response.getDocument().getSingleData().get().getType()).isEqualTo("tasks");

		// eager loading but no inclusion
		RegistryEntry entry = resourceRegistry.getEntry(Task.class);
		ResourceField projectsField = entry.getResourceInformation().findFieldByUnderlyingName("projects");
		Assert.assertEquals(SerializeType.ONLY_ID, projectsField.getSerializeType());
		assertThat(response.getDocument().getSingleData().get().getRelationships().get("projects").getData().isPresent()).isTrue();
	}
}
