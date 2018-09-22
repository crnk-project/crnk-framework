package io.crnk.core.engine.internal.dispatcher.controller.collection;

import io.crnk.core.engine.dispatcher.Response;
import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.document.Relationship;
import io.crnk.core.engine.document.Resource;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.internal.dispatcher.controller.BaseControllerTest;
import io.crnk.core.engine.internal.dispatcher.controller.CollectionGetController;
import io.crnk.core.engine.internal.dispatcher.controller.RelationshipsResourcePostController;
import io.crnk.core.engine.internal.dispatcher.controller.ResourceGetController;
import io.crnk.core.engine.internal.dispatcher.controller.ResourcePostController;
import io.crnk.core.engine.internal.dispatcher.path.JsonPath;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.mock.models.Project;
import io.crnk.core.mock.models.Task;
import io.crnk.core.mock.repository.TaskToProjectRepository;
import io.crnk.core.queryspec.PathSpec;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.resource.annotations.SerializeType;
import io.crnk.core.utils.Nullable;
import io.crnk.legacy.queryParams.QueryParams;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class CollectionGetControllerTest extends BaseControllerTest {

	private static final String REQUEST_TYPE = "GET";


	@Test
	public void onGivenRequestCollectionGetShouldAcceptIt() {
		// GIVEN
		JsonPath jsonPath = pathBuilder.build("/tasks/");
		CollectionGetController sut = new CollectionGetController();
		sut.init(controllerContext);

		// WHEN
		boolean result = sut.isAcceptable(jsonPath, REQUEST_TYPE);

		// THEN
		Assert.assertEquals(result, true);
	}

	@Test
	public void onGivenRequestCollectionGetShouldDenyIt() {
		// GIVEN
		JsonPath jsonPath = pathBuilder.build("/tasks/2");
		CollectionGetController sut = new CollectionGetController();
		sut.init(controllerContext);

		// WHEN
		boolean result = sut.isAcceptable(jsonPath, REQUEST_TYPE);

		// THEN
		Assert.assertEquals(result, false);
	}

	@Test
	public void onGivenRequestCollectionGetShouldHandleIt() {
		// GIVEN

		JsonPath jsonPath = pathBuilder.build("/tasks/");
		CollectionGetController sut = new CollectionGetController();
		sut.init(controllerContext);

		// WHEN
		Response response = sut.handle(jsonPath, emptyTaskQuery, null);

		// THEN
		Assert.assertNotNull(response);
	}

	@Test
	public void onGivenRequestCollectionWithIdsGetShouldHandleIt() {
		// GIVEN

		JsonPath jsonPath = pathBuilder.build("/tasks/1,2");
		CollectionGetController sut = new CollectionGetController();
		sut.init(controllerContext);

		// WHEN
		Response response = sut.handle(jsonPath, emptyTaskQuery, null);

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
		ResourcePostController resourcePost = new ResourcePostController();
		resourcePost.init(controllerContext);

		// WHEN -- adding a task
		Response taskResponse = resourcePost.handle(taskPath, emptyTaskQuery, Document);

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
		ResourcePostController resourcePost = new ResourcePostController();
		resourcePost.init(controllerContext);

		// WHEN -- adding a task
		Response taskResponse = resourcePost.handle(taskPath, emptyTaskQuery, newTaskBody);

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
		Response projectResponse = resourcePost.handle(projectPath, emptyProjectQuery, newProjectBody);

		// THEN
		assertThat(projectResponse.getDocument().getSingleData().get().getType()).isEqualTo("projects");
		assertThat(projectResponse.getDocument().getSingleData().get().getId()).isNotNull();
		assertThat(projectResponse.getDocument().getSingleData().get().getAttributes().get("name").asText())
				.isEqualTo("sample project");
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
		RelationshipsResourcePostController sut = new RelationshipsResourcePostController();
		sut.init(controllerContext);

		// WHEN -- adding a relation between task and project
		Response projectRelationshipResponse = sut.handle(savedTaskPath, emptyProjectQuery, newTaskToProjectBody);
		assertThat(projectRelationshipResponse).isNotNull();

		// THEN
		TaskToProjectRepository taskToProjectRepository = new TaskToProjectRepository();
		Project project = taskToProjectRepository.findOneTarget(taskId, "includedProjects", new QueryParams());
		assertThat(project.getId()).isEqualTo(projectId);

		// Given
		JsonPath jsonPath = pathBuilder.build("/tasks/" + taskId);
		ResourceGetController responseGetResp = new ResourceGetController();
		responseGetResp.init(controllerContext);
		QuerySpec queryParams1 = new QuerySpec(Task.class);
		queryParams1.includeRelation(PathSpec.of("includedProjects"));

		// WHEN
		Response response = responseGetResp.handle(jsonPath,
				container.toQueryAdapter(queryParams1), null);

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
		ResourcePostController resourcePost = new ResourcePostController();
		resourcePost.init(controllerContext);

		// WHEN -- adding a task
		Response taskResponse = resourcePost.handle(taskPath, emptyTaskQuery, newTaskBody);

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
		Response projectResponse = resourcePost.handle(projectPath, emptyProjectQuery, newProjectBody);

		// THEN
		assertThat(projectResponse.getDocument().getSingleData().get().getType()).isEqualTo("projects");
		assertThat(projectResponse.getDocument().getSingleData().get().getId()).isNotNull();
		assertThat(projectResponse.getDocument().getSingleData().get().getAttributes().get("name").asText())
				.isEqualTo("sample project");
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
		RelationshipsResourcePostController sut =
				new RelationshipsResourcePostController();
		sut.init(controllerContext);

		// WHEN -- adding a relation between task and project
		Response projectRelationshipResponse = sut.handle(savedTaskPath, emptyProjectQuery, newTaskToProjectBody);
		assertThat(projectRelationshipResponse).isNotNull();

		// THEN
		TaskToProjectRepository taskToProjectRepository = new TaskToProjectRepository();
		Project project = taskToProjectRepository.findOneTarget(taskId, "projects", new QueryParams());
		assertThat(project.getId()).isNotNull();

		// Given
		JsonPath jsonPath = pathBuilder.build("/tasks/" + taskId);
		ResourceGetController responseGetResp = new ResourceGetController();
		responseGetResp.init(controllerContext);
		QuerySpec requestParams = new QuerySpec(Task.class);
		requestParams.includeRelation(PathSpec.of("projects"));

		// WHEN
		Response response = responseGetResp.handle(jsonPath,
				container.toQueryAdapter(requestParams), null);

		// THEN
		Assert.assertNotNull(response);
		assertThat(response.getDocument().getSingleData().get().getType()).isEqualTo("tasks");

		// eager loading but no inclusion
		RegistryEntry entry = resourceRegistry.getEntry(Task.class);
		ResourceField projectsField = entry.getResourceInformation().findFieldByUnderlyingName("projects");
		Assert.assertEquals(SerializeType.ONLY_ID, projectsField.getSerializeType());
		assertThat(response.getDocument().getSingleData().get().getRelationships().get("projects").getData().isPresent())
				.isTrue();
	}
}
