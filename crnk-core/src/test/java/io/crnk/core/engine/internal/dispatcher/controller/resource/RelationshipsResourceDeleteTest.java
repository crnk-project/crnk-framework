package io.crnk.core.engine.internal.dispatcher.controller.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.core.engine.dispatcher.Response;
import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.document.Resource;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.http.HttpStatus;
import io.crnk.core.engine.internal.dispatcher.controller.BaseControllerTest;
import io.crnk.core.engine.internal.dispatcher.controller.RelationshipsResourceDelete;
import io.crnk.core.engine.internal.dispatcher.controller.RelationshipsResourcePost;
import io.crnk.core.engine.internal.dispatcher.controller.ResourcePost;
import io.crnk.core.engine.internal.dispatcher.path.JsonPath;
import io.crnk.core.engine.internal.dispatcher.path.ResourcePath;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.mock.models.Project;
import io.crnk.core.mock.models.User;
import io.crnk.core.mock.repository.TaskToProjectRepository;
import io.crnk.core.mock.repository.UserToProjectRepository;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.queryspec.internal.QuerySpecAdapter;
import io.crnk.core.utils.Nullable;
import io.crnk.legacy.queryParams.QueryParams;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class RelationshipsResourceDeleteTest extends BaseControllerTest {

	private static final String REQUEST_TYPE = HttpMethod.DELETE.name();

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private static final QueryParams REQUEST_PARAMS = new QueryParams();


	@Test
	public void onValidRequestShouldAcceptIt() {
		// GIVEN
		JsonPath jsonPath = pathBuilder.build("tasks/1/relationships/project");
		ResourceRegistry resourceRegistry = mock(ResourceRegistry.class);
		RelationshipsResourceDelete sut = new RelationshipsResourceDelete(resourceRegistry, typeParser);

		// WHEN
		boolean result = sut.isAcceptable(jsonPath, REQUEST_TYPE);

		// THEN
		assertThat(result).isTrue();
	}

	@Test
	public void onNonRelationRequestShouldDenyIt() {
		// GIVEN
		JsonPath jsonPath = new ResourcePath("tasks");
		ResourceRegistry resourceRegistry = mock(ResourceRegistry.class);
		RelationshipsResourceDelete sut = new RelationshipsResourceDelete(resourceRegistry, typeParser);

		// WHEN
		boolean result = sut.isAcceptable(jsonPath, REQUEST_TYPE);

		// THEN
		assertThat(result).isFalse();
	}

	@Test
	public void onExistingToOneRelationshipShouldRemoveIt() throws Exception {
		// GIVEN
		Document newTaskBody = new Document();
		Resource data = createTask();
		newTaskBody.setData(Nullable.of((Object) data));
		data.setType("tasks");

		JsonPath taskPath = pathBuilder.build("/tasks");
		ResourcePost resourcePost = new ResourcePost(resourceRegistry, PROPERTIES_PROVIDER, typeParser, OBJECT_MAPPER, documentMapper);

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
		newTaskToProjectBody.setData(Nullable.of((Object) data));
		data.setType("projects");
		data.setId(projectId.toString());

		JsonPath projectRelationPath = pathBuilder.build("/tasks/" + taskId + "/relationships/project");
		RelationshipsResourcePost relationshipsResourcePost = new RelationshipsResourcePost(resourceRegistry, typeParser);

		// WHEN -- adding a relation between task and project
		Response projectRelationshipResponse = relationshipsResourcePost.handle(projectRelationPath, emptyProjectQuery, null, newTaskToProjectBody);
		assertThat(projectRelationshipResponse).isNotNull();

		// THEN
		TaskToProjectRepository taskToProjectRepository = new TaskToProjectRepository();
		Project project = taskToProjectRepository.findOneTarget(taskId, "project", REQUEST_PARAMS);
		assertThat(project.getId()).isEqualTo(projectId);

		/* ------- */

		// GIVEN
		RelationshipsResourceDelete sut = new RelationshipsResourceDelete(resourceRegistry, typeParser);

		// WHEN -- removing a relation between task and project
		Response result = sut.handle(projectRelationPath, emptyProjectQuery, null, newTaskToProjectBody);
		assertThat(result).isNotNull();
		taskToProjectRepository.removeRelations("project");

		// THEN
		assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.NO_CONTENT_204);
		Project nullProject = taskToProjectRepository.findOneTarget(taskId, "project", REQUEST_PARAMS);
		assertThat(nullProject).isNull();
	}

	@Test
	public void onExistingToManyRelationshipShouldRemoveIt() throws Exception {
		// GIVEN
		Document newUserDocument = new Document();
		newUserDocument.setData(Nullable.of((Object) createUser()));

		JsonPath taskPath = pathBuilder.build("/users");
		ResourcePost resourcePost = new ResourcePost(resourceRegistry, PROPERTIES_PROVIDER, typeParser, OBJECT_MAPPER, documentMapper);

		// WHEN -- adding a user
		Response taskResponse = resourcePost.handle(taskPath, new QuerySpecAdapter(new QuerySpec(User.class), resourceRegistry), null, newUserDocument);

		// THEN
		assertThat(taskResponse.getDocument().getSingleData().get().getType()).isEqualTo("users");
		Long userId = Long.parseLong(taskResponse.getDocument().getSingleData().get().getId());
		assertThat(userId).isNotNull();

		/* ------- */

		// GIVEN
		Document newProjectDocument = new Document();
		newProjectDocument.setData(Nullable.of((Object) createProject()));

		JsonPath projectPath = pathBuilder.build("/projects");

		// WHEN -- adding a project
		Response projectResponse = resourcePost.handle(projectPath, emptyProjectQuery, null, newProjectDocument);

		// THEN
		assertThat(projectResponse.getDocument().getSingleData().get().getType()).isEqualTo("projects");
		assertThat(projectResponse.getDocument().getSingleData().get().getId()).isNotNull();
		assertThat(projectResponse.getDocument().getSingleData().get().getAttributes().get("name").asText()).isEqualTo("sample project");
		Long projectId = Long.parseLong(projectResponse.getDocument().getSingleData().get().getId());
		assertThat(projectId).isNotNull();

		/* ------- */

		// GIVEN
		Document newProjectDocument2 = new Document();
		newProjectDocument2.setData(Nullable.of((Object) createProject(projectId.toString())));

		JsonPath savedTaskPath = pathBuilder.build("/users/" + userId + "/relationships/assignedProjects");
		RelationshipsResourcePost relationshipsResourcePost = new RelationshipsResourcePost(resourceRegistry, typeParser);

		// WHEN -- adding a relation between user and project
		Response projectRelationshipResponse = relationshipsResourcePost.handle(savedTaskPath, emptyProjectQuery, null, newProjectDocument2);
		assertThat(projectRelationshipResponse).isNotNull();

		// THEN
		UserToProjectRepository userToProjectRepository = new UserToProjectRepository();
		Project project = userToProjectRepository.findOneTarget(userId, "assignedProjects", new QuerySpec(Project.class));
		assertThat(project.getId()).isEqualTo(projectId);

		/* ------- */

		// GIVEN
		RelationshipsResourceDelete sut = new RelationshipsResourceDelete(resourceRegistry, typeParser);

		// WHEN -- removing a relation between task and project
		Response result = sut.handle(savedTaskPath, emptyProjectQuery, null, newProjectDocument2);
		assertThat(result).isNotNull();

		// THEN
		Project nullProject = userToProjectRepository.findOneTarget(userId, "assignedProjects", new QuerySpec(Project.class));
		assertThat(nullProject).isNull();
	}
}
