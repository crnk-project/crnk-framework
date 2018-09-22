package io.crnk.core.engine.internal.dispatcher.controller.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.core.engine.dispatcher.Response;
import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.document.Relationship;
import io.crnk.core.engine.document.Resource;
import io.crnk.core.engine.document.ResourceIdentifier;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.http.HttpStatus;
import io.crnk.core.engine.internal.dispatcher.controller.BaseControllerTest;
import io.crnk.core.engine.internal.dispatcher.controller.RelationshipsResourcePatchController;
import io.crnk.core.engine.internal.dispatcher.controller.ResourcePatchController;
import io.crnk.core.engine.internal.dispatcher.controller.ResourcePostController;
import io.crnk.core.engine.internal.dispatcher.path.JsonPath;
import io.crnk.core.engine.internal.utils.ClassUtils;
import io.crnk.core.mock.models.Project;
import io.crnk.core.mock.models.ProjectPolymorphic;
import io.crnk.core.mock.models.Task;
import io.crnk.core.mock.repository.TaskToProjectRepository;
import io.crnk.core.mock.repository.UserToProjectRepository;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepositoryV2;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.core.utils.Nullable;
import io.crnk.legacy.queryParams.QueryParams;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class RelationshipsResourcePatchControllerControllerTest extends BaseControllerTest {

	private static final String REQUEST_TYPE = HttpMethod.PATCH.name();

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private static final QueryParams REQUEST_PARAMS = new QueryParams();

	private UserToProjectRepository localUserToProjectRepository;

	@Before
	public void beforeTest() {
		localUserToProjectRepository = new UserToProjectRepository();
		localUserToProjectRepository.removeRelations("project");
		localUserToProjectRepository.removeRelations("assignedProjects");
	}

	@Test
	public void onValidRequestShouldAcceptIt() {
		// GIVEN
		JsonPath jsonPath = pathBuilder.build("tasks/1/relationships/project");
		RelationshipsResourcePatchController sut = new RelationshipsResourcePatchController();
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
		RelationshipsResourcePatchController sut = new RelationshipsResourcePatchController();
		sut.init(controllerContext);

		// WHEN
		boolean result = sut.isAcceptable(jsonPath, REQUEST_TYPE);

		// THEN
		assertThat(result).isFalse();
	}

	@Test
	public void onExistingResourcesShouldAddToOneRelationship() {
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
		assertThat(projectResponse.getDocument().getSingleData().get().getAttributes().get("name").asText())
				.isEqualTo("sample project");
		Long projectId = Long.parseLong(projectResponse.getDocument().getSingleData().get().getId());
		assertThat(projectId).isNotNull();

		/* ------- */

		// GIVEN
		Document newTaskToProjectBody = new Document();
		data = new Resource();
		newTaskToProjectBody.setData(Nullable.of(data));
		data.setType("projects");
		data.setId(projectId.toString());

		JsonPath savedTaskPath = pathBuilder.build("/tasks/" + taskId + "/relationships/project");
		RelationshipsResourcePatchController sut = new RelationshipsResourcePatchController();
		sut.init(controllerContext);

		// WHEN -- adding a relation between task and project
		Response projectRelationshipResponse = sut.handle(savedTaskPath, emptyProjectQuery, newTaskToProjectBody);
		assertThat(projectRelationshipResponse).isNotNull();

		// THEN
		TaskToProjectRepository taskToProjectRepository = new TaskToProjectRepository();
		Project project = taskToProjectRepository.findOneTarget(taskId, "project", REQUEST_PARAMS);
		assertThat(project.getId()).isEqualTo(projectId);
	}

	@Test
	public void onExistingResourcesShouldAddToManyRelationship() {
		// GIVEN
		Document newUserBody = new Document();
		Resource data = createUser();
		newUserBody.setData(Nullable.of(data));

		JsonPath taskPath = pathBuilder.build("/users");
		ResourcePostController resourcePost = new ResourcePostController();
		resourcePost.init(controllerContext);

		// WHEN -- adding a user
		Response taskResponse = resourcePost.handle(taskPath, emptyUserQuery, newUserBody);

		// THEN
		assertThat(taskResponse.getDocument().getSingleData().get().getType()).isEqualTo("users");
		Long userId = Long.parseLong(taskResponse.getDocument().getSingleData().get().getId());
		assertThat(userId).isNotNull();

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
		assertThat(projectResponse.getDocument().getSingleData().get().getAttributes().get("name").asText())
				.isEqualTo("sample project");
		Long projectId = Long.parseLong(projectResponse.getDocument().getSingleData().get().getId());
		assertThat(projectId).isNotNull();

		/* ------- */

		// GIVEN
		Document newTaskToProjectBody = new Document();
		data = createProject();
		data.setId(projectId.toString());
		newTaskToProjectBody.setData(Nullable.of(Collections.singletonList(data)));

		JsonPath savedTaskPath = pathBuilder.build("/users/" + userId + "/relationships/assignedProjects");
		RelationshipsResourcePatchController sut = new RelationshipsResourcePatchController();
		sut.init(controllerContext);

		// WHEN -- adding a relation between user and project
		Response projectRelationshipResponse = sut.handle(savedTaskPath, emptyProjectQuery, newTaskToProjectBody);
		assertThat(projectRelationshipResponse).isNotNull();

		// THEN
		UserToProjectRepository userToProjectRepository = new UserToProjectRepository();
		Project project = userToProjectRepository.findOneTarget(userId, "assignedProjects", new QuerySpec(Project.class));
		assertThat(project.getId()).isEqualTo(projectId);
	}

	@Test
	public void onDeletingToOneRelationshipShouldSetTheValue() {
		// GIVEN
		Document newTaskBody = new Document();
		Resource data = createTask();
		newTaskBody.setData(Nullable.of(data));

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
		Document newTaskToProjectBody = new Document();
		newTaskToProjectBody.setData(Nullable.nullValue());

		JsonPath savedTaskPath = pathBuilder.build("/tasks/" + taskId + "/relationships/project");
		RelationshipsResourcePatchController sut = new RelationshipsResourcePatchController();
		sut.init(controllerContext);

		// WHEN -- adding a relation between user and project
		Response projectRelationshipResponse = sut.handle(savedTaskPath, emptyProjectQuery, newTaskToProjectBody);
		assertThat(projectRelationshipResponse).isNotNull();

		// THEN
		assertThat(projectRelationshipResponse.getHttpStatus()).isEqualTo(HttpStatus.NO_CONTENT_204);
		Project project = localUserToProjectRepository.findOneTarget(1L, "project", new QuerySpec(Project.class));
		assertThat(project).isNull();
	}

	@Test
	public void supportPolymorphicRelationshipTypes() {

		// GIVEN
		Document newTaskBody = new Document();
		Resource data = new Resource();
		data.setType(ClassUtils.getAnnotation(Task.class, JsonApiResource.class).get().type());
		newTaskBody.setData(Nullable.of(data));

		JsonPath taskPath = pathBuilder.build("/tasks");

		ResourcePostController resourcePost = new ResourcePostController();
		resourcePost.init(controllerContext);
		Response taskResponse = resourcePost.handle(taskPath, emptyTaskQuery, newTaskBody);
		assertThat(taskResponse.getDocument().getSingleData().get().getType()).isEqualTo("tasks");
		Long taskIdOne = Long.parseLong(taskResponse.getDocument().getSingleData().get().getId());
		assertThat(taskIdOne).isNotNull();
		taskResponse = resourcePost.handle(taskPath, emptyTaskQuery, newTaskBody);
		Long taskIdTwo = Long.parseLong(taskResponse.getDocument().getSingleData().get().getId());
		assertThat(taskIdOne).isNotNull();
		taskResponse = resourcePost.handle(taskPath, emptyTaskQuery, newTaskBody);
		Long taskIdThree = Long.parseLong(taskResponse.getDocument().getSingleData().get().getId());
		assertThat(taskIdOne).isNotNull();
		newTaskBody = new Document();

		// Create ProjectPolymorphic object
		Document newProjectBody = new Document();
		data = new Resource();
		String type = ClassUtils.getAnnotation(ProjectPolymorphic.class, JsonApiResource.class).get().type();
		data.setType(type);
		data.getRelationships().put("task", new Relationship(new ResourceIdentifier(taskIdOne.toString(), "tasks")));
		data.getRelationships().put("tasks", new Relationship(Arrays.asList(new ResourceIdentifier(taskIdTwo.toString(),
						"tasks"),
				new ResourceIdentifier(taskIdThree.toString(), "tasks"))));
		newProjectBody.setData(Nullable.of(data));
		JsonPath projectPolymorphicTypePath = pathBuilder.build("/" + type);

		Response projectResponse = resourcePost.handle(projectPolymorphicTypePath, emptyProjectQuery, newProjectBody);

		assertThat(projectResponse.getDocument().getSingleData().get().getType()).isEqualTo("projects-polymorphic");
		Long projectId = Long.parseLong(projectResponse.getDocument().getSingleData().get().getId());
		assertThat(projectId).isNotNull();
		Resource projectPolymorphic = projectResponse.getDocument().getSingleData().get();
		assertNotNull(projectPolymorphic.getRelationships().get("task").getSingleData().get());
		assertNotNull(projectPolymorphic.getRelationships().get("tasks"));

		ResourceRepositoryV2 resourceRepository =
				resourceRegistry.getEntry(ProjectPolymorphic.class).getResourceRepositoryFacade();
		ProjectPolymorphic projectPolymorphicObj =
				(ProjectPolymorphic) resourceRepository.findOne(projectId, new QuerySpec(ProjectPolymorphic.class));
		assertEquals(2, projectPolymorphicObj.getTasks().size());

		projectPolymorphicTypePath = pathBuilder.build("/" + type + "/" + projectPolymorphic.getId());
		ResourcePatchController resourcePatch = new ResourcePatchController();
		resourcePatch.init(controllerContext);
		data = newProjectBody.getSingleData().get();
		data.setId(projectId.toString());
		projectPolymorphic.setId(Long.toString(projectId));
		data.getRelationships().get("tasks").setData(Nullable.of(new ArrayList<ResourceIdentifier>()));

		// WHEN
		Response baseResponseContext = resourcePatch.handle(projectPolymorphicTypePath,
				container.toQueryAdapter(new QuerySpec(ProjectPolymorphic.class)), newProjectBody);
		assertThat(baseResponseContext.getDocument().getSingleData().get().getType()).isEqualTo("projects-polymorphic");
		projectId = Long.parseLong(baseResponseContext.getDocument().getSingleData().get().getId());
		assertThat(projectId).isNotNull();
		projectPolymorphic = baseResponseContext.getDocument().getSingleData().get();
		assertNotNull(projectPolymorphic.getRelationships().get("task").getSingleData().get());
		assertNotNull(projectPolymorphic.getRelationships().get("tasks"));

		projectPolymorphicObj =
				(ProjectPolymorphic) resourceRepository.findOne(projectId, new QuerySpec(ProjectPolymorphic.class));
		assertEquals(0, projectPolymorphicObj.getTasks().size());
	}
}
