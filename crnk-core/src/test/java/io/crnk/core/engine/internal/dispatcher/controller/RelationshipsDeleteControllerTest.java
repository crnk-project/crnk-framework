package io.crnk.core.engine.internal.dispatcher.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.core.engine.dispatcher.Response;
import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.document.Resource;
import io.crnk.core.engine.document.ResourceIdentifier;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.http.HttpStatus;
import io.crnk.core.engine.internal.dispatcher.path.JsonPath;
import io.crnk.core.exception.ForbiddenException;
import io.crnk.core.mock.models.Project;
import io.crnk.core.mock.models.Task;
import io.crnk.core.mock.models.User;
import io.crnk.core.mock.repository.TaskRepository;
import io.crnk.core.mock.repository.TaskToProjectRepository;
import io.crnk.core.mock.repository.UserToProjectRepository;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.utils.Nullable;
import io.crnk.legacy.queryParams.QueryParams;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class RelationshipsDeleteControllerTest extends ControllerTestBase {

	private static final String REQUEST_TYPE = HttpMethod.DELETE.name();

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private static final QueryParams REQUEST_PARAMS = new QueryParams();


	@Test
	public void onValidRequestShouldAcceptIt() {
		// GIVEN
		JsonPath jsonPath = pathBuilder.build("tasks/1/relationships/project");
		RelationsDeleteController sut = new RelationsDeleteController();
		sut.init(controllerContext);

		// WHEN
		boolean result = sut.isAcceptable(jsonPath, REQUEST_TYPE);

		// THEN
		assertThat(result).isTrue();
	}

	@Test
	public void onNonRelationRequestShouldDenyIt() {
		// GIVEN
		JsonPath jsonPath = pathBuilder.build("tasks");
		RelationsDeleteController sut = new RelationsDeleteController();
		sut.init(controllerContext);

		// WHEN
		boolean result = sut.isAcceptable(jsonPath, REQUEST_TYPE);

		// THEN
		assertThat(result).isFalse();
	}

	@Test
	public void onExistingToOneRelationshipShouldRemoveIt() {
		// GIVEN
		Document newTaskBody = new Document();
		Resource data = createTask();
		newTaskBody.setData(Nullable.of(data));
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
		newProjectBody.setData(Nullable.of(data));

		JsonPath projectPath = pathBuilder.build("/projects");

		// WHEN -- adding a project
		Response projectResponse = resourcePost.handle(projectPath, emptyProjectQuery, newProjectBody);

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
		newTaskToProjectBody.setData(Nullable.of(data));
		data.setType("projects");
		data.setId(projectId.toString());

		JsonPath projectRelationPath = pathBuilder.build("/tasks/" + taskId + "/relationships/project");
		RelationshipsPostController relationshipsPostController = new RelationshipsPostController();
		relationshipsPostController.init(controllerContext);

		// WHEN -- adding a relation between task and project
		Response projectRelationshipResponse = relationshipsPostController.handle(projectRelationPath, emptyProjectQuery, newTaskToProjectBody);
		assertThat(projectRelationshipResponse).isNotNull();

		// THEN
		TaskToProjectRepository taskToProjectRepository = new TaskToProjectRepository();
		Project project = taskToProjectRepository.findOneTarget(taskId, "project", REQUEST_PARAMS);
		assertThat(project.getId()).isEqualTo(projectId);

		/* ------- */

		// GIVEN
		RelationsDeleteController sut = new RelationsDeleteController();
		sut.init(controllerContext);

		// WHEN -- removing a relation between task and project
		Response result = sut.handle(projectRelationPath, emptyProjectQuery, newTaskToProjectBody);
		assertThat(result).isNotNull();
		taskToProjectRepository.removeRelations("project");

		// THEN
		assertThat(result.getHttpStatus()).isEqualTo(HttpStatus.NO_CONTENT_204);
		Project nullProject = taskToProjectRepository.findOneTarget(taskId, "project", REQUEST_PARAMS);
		assertThat(nullProject).isNull();
	}

	@Test
	public void onExistingToManyRelationshipShouldRemoveIt() {
		// GIVEN
		Document newUserDocument = new Document();
		newUserDocument.setData(Nullable.of(createUser()));

		JsonPath taskPath = pathBuilder.build("/users");
		ResourcePostController resourcePost = new ResourcePostController();
		resourcePost.init(controllerContext);

		// WHEN -- adding a user
		Response taskResponse = resourcePost.handle(taskPath, container.toQueryAdapter(new QuerySpec(User.class)), newUserDocument);

		// THEN
		assertThat(taskResponse.getDocument().getSingleData().get().getType()).isEqualTo("users");
		Long userId = Long.parseLong(taskResponse.getDocument().getSingleData().get().getId());
		assertThat(userId).isNotNull();

		/* ------- */

		// GIVEN
		Document newProjectDocument = new Document();
		newProjectDocument.setData(Nullable.of(createProject()));

		JsonPath projectPath = pathBuilder.build("/projects");

		// WHEN -- adding a project
		Response projectResponse = resourcePost.handle(projectPath, emptyProjectQuery, newProjectDocument);

		// THEN
		assertThat(projectResponse.getDocument().getSingleData().get().getType()).isEqualTo("projects");
		assertThat(projectResponse.getDocument().getSingleData().get().getId()).isNotNull();
		assertThat(projectResponse.getDocument().getSingleData().get().getAttributes().get("name").asText()).isEqualTo("sample project");
		Long projectId = Long.parseLong(projectResponse.getDocument().getSingleData().get().getId());
		assertThat(projectId).isNotNull();

		/* ------- */

		// GIVEN
		Document newProjectDocument2 = new Document();
		newProjectDocument2.setData(Nullable.of(createProject(projectId.toString())));

		JsonPath savedTaskPath = pathBuilder.build("/users/" + userId + "/relationships/assignedProjects");
		RelationshipsPostController relationshipsPostController = new RelationshipsPostController();
		relationshipsPostController.init(controllerContext);

		// WHEN -- adding a relation between user and project
		Response projectRelationshipResponse = relationshipsPostController.handle(savedTaskPath, emptyProjectQuery, newProjectDocument2);
		assertThat(projectRelationshipResponse).isNotNull();

		// THEN
		UserToProjectRepository userToProjectRepository = new UserToProjectRepository();
		Project project = userToProjectRepository.findOneTarget(userId, "assignedProjects", new QuerySpec(Project.class));
		assertThat(project.getId()).isEqualTo(projectId);

		/* ------- */

		// GIVEN
		RelationsDeleteController sut = new RelationsDeleteController();
		sut.init(controllerContext);

		// WHEN -- removing a relation between task and project
		Response result = sut.handle(savedTaskPath, emptyProjectQuery, newProjectDocument2);
		assertThat(result).isNotNull();

		// THEN
		Project nullProject = userToProjectRepository.findOneTarget(userId, "assignedProjects", new QuerySpec(Project.class));
		assertThat(nullProject).isNull();
	}

	@Test
	public void onNonDeletableRelationshipShouldThrowException() {
		Task task = new Task();
		task.setName("some task");
		TaskRepository taskRepository = new TaskRepository();
		taskRepository.save(task);
		Long taskId = task.getId();

		// attempt to update non-postable relationship
		Document body = new Document();
		ResourceIdentifier id = new ResourceIdentifier("13", "things");
		body.setData(Nullable.of(id));
		JsonPath savedTaskPath = pathBuilder.build("/tasks/" + taskId + "/relationships/statusThing");
		RelationsDeleteController sut = new RelationsDeleteController();
		sut.init(controllerContext);

		// WHEN -- adding a relation between user and project
		assertThatThrownBy(() -> sut.handle(savedTaskPath, emptyTaskQuery, body))
				.isInstanceOf(ForbiddenException.class)
				.hasMessage("field 'tasks.statusThing' cannot be accessed for DELETE");
	}
}
