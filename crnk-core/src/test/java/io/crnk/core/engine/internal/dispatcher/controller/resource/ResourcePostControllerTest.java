package io.crnk.core.engine.internal.dispatcher.controller.resource;

import com.fasterxml.jackson.databind.JsonNode;
import io.crnk.core.boot.CrnkProperties;
import io.crnk.core.engine.dispatcher.Response;
import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.document.Relationship;
import io.crnk.core.engine.document.Resource;
import io.crnk.core.engine.document.ResourceIdentifier;
import io.crnk.core.engine.filter.ResourceRelationshipModificationType;
import io.crnk.core.engine.http.HttpStatus;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.internal.dispatcher.controller.BaseControllerTest;
import io.crnk.core.engine.internal.dispatcher.controller.ResourcePostController;
import io.crnk.core.engine.internal.dispatcher.path.JsonPath;
import io.crnk.core.engine.properties.PropertiesProvider;
import io.crnk.core.engine.properties.ResourceFieldImmutableWriteBehavior;
import io.crnk.core.exception.BadRequestException;
import io.crnk.core.exception.ForbiddenException;
import io.crnk.core.exception.RequestBodyException;
import io.crnk.core.exception.RequestBodyNotFoundException;
import io.crnk.core.exception.ResourceException;
import io.crnk.core.mock.models.Pojo;
import io.crnk.core.mock.models.Task;
import io.crnk.core.mock.repository.ProjectRepository;
import io.crnk.core.mock.repository.TaskRepository;
import io.crnk.core.queryspec.PathSpec;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepositoryV2;
import io.crnk.core.utils.Nullable;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ResourcePostControllerTest extends BaseControllerTest {

	private static final String REQUEST_TYPE = "POST";

	@Test
	public void onGivenRequestCollectionGetShouldDenyIt() {
		// GIVEN
		JsonPath jsonPath = pathBuilder.build("/tasks/1");
		ResourcePostController sut = new ResourcePostController();
		sut.init(controllerContext);

		// WHEN
		boolean result = sut.isAcceptable(jsonPath, REQUEST_TYPE);

		// THEN
		Assert.assertEquals(result, false);
	}

	@Test
	public void onGivenRequestResourceGetShouldAcceptIt() {
		// GIVEN
		JsonPath jsonPath = pathBuilder.build("/tasks/");
		ResourcePostController sut = new ResourcePostController();
		sut.init(controllerContext);

		// WHEN
		boolean result = sut.isAcceptable(jsonPath, REQUEST_TYPE);

		// THEN
		Assert.assertEquals(result, true);
	}

	@Test
	public void onInconsistentResourceTypesShouldThrowException() {
		// GIVEN
		Document newProjectBody = new Document();
		Resource data = createProject();
		newProjectBody.setData(Nullable.of(data));

		JsonPath projectPath = pathBuilder.build("/tasks");
		ResourcePostController sut = new ResourcePostController();
		sut.init(controllerContext);

		// THEN
		expectedException.expect(RuntimeException.class);

		// WHEN
		sut.handle(projectPath, emptyTaskQuery, newProjectBody);
	}

	@Test
	public void onMultipleDataThrowException() {
		// GIVEN
		Document newProjectBody = new Document();
		Resource data = createProject();
		newProjectBody.setData(Nullable.of(new ArrayList<>()));

		JsonPath projectPath = pathBuilder.build("/tasks");
		ResourcePostController sut = new ResourcePostController();
		sut.init(controllerContext);

		// THEN
		expectedException.expect(RequestBodyException.class);

		// WHEN
		sut.handle(projectPath, emptyTaskQuery, newProjectBody);
	}


	@Test
	public void onNoBodyResourceShouldThrowException() {
		// GIVEN
		ResourcePostController sut = new ResourcePostController();
		sut.init(controllerContext);

		// THEN
		expectedException.expect(RequestBodyNotFoundException.class);

		// WHEN
		JsonPath path = pathBuilder.build("tasks");
		sut.handle(path, emptyTaskQuery, null);
	}

	@Test
	public void onNewResourcesAndRelationshipShouldPersistThoseData() {
		// GIVEN
		Document newProjectBody = new Document();
		Resource data = createProject();
		newProjectBody.setData(Nullable.of(data));

		JsonPath projectPath = pathBuilder.build("/projects");
		ResourcePostController sut = new ResourcePostController();
		sut.init(controllerContext);

		// WHEN
		Response projectResponse = sut.handle(projectPath, emptyProjectQuery, newProjectBody);

		// THEN
		assertThat(projectResponse.getHttpStatus()).isEqualTo(HttpStatus.CREATED_201);
		assertThat(projectResponse.getDocument().getData().get()).isExactlyInstanceOf(Resource.class);
		assertThat(projectResponse.getDocument().getSingleData().get().getType()).isEqualTo("projects");
		Resource persistedProject = projectResponse.getDocument().getSingleData().get();
		assertThat(persistedProject.getId()).isNotNull();
		assertThat(persistedProject.getAttributes().get("name").asText()).isEqualTo("sample project");
		assertThat(persistedProject.getAttributes().get("data").get("data").asText()).isEqualTo("asd");
		Long projectId = Long.parseLong(projectResponse.getDocument().getSingleData().get().getId());
		Mockito.verify(modificationFilter, Mockito.times(1))
				.modifyAttribute(Mockito.any(), Mockito.any(ResourceField.class), Mockito.eq("data"), Mockito.any());

		/* ------- */

		// GIVEN
		Document newTasksBody = new Document();
		newTasksBody.setData(Nullable.of(createTask()));
		newTasksBody.getSingleData().get().getRelationships()
				.put("project", new Relationship(new ResourceIdentifier(projectId.toString(), "projects")));

		JsonPath taskPath = pathBuilder.build("/tasks");

		// WHEN
		Response taskResponse = sut.handle(taskPath, emptyTaskQuery, newTasksBody);

		// THEN
		assertThat(taskResponse.getHttpStatus()).isEqualTo(HttpStatus.CREATED_201);
		assertThat(taskResponse.getDocument().getSingleData().get().getType()).isEqualTo("tasks");
		String taskId = taskResponse.getDocument().getSingleData().get().getId();
		assertThat(taskId).isNotNull();
		assertThat(taskResponse.getDocument().getSingleData().get().getAttributes().get("name").asText())
				.isEqualTo("sample task");

		TaskRepository taskRepository = new TaskRepository();
		Task persistedTask = taskRepository.findOne(Long.parseLong(taskId), null);
		assertThat(persistedTask.getProject().getId()).isEqualTo(projectId);
	}

	@Test
	public void onRepositoryReturnNullShouldThrowException() {
		// GIVEN
		Document newProjectBody = new Document();
		Resource data = createProject();
		data.setId(Long.toString(ProjectRepository.RETURN_NULL_ON_CREATE_ID));
		newProjectBody.setData(Nullable.of(data));

		JsonPath projectPath = pathBuilder.build("/projects");
		ResourcePostController sut = new ResourcePostController();
		sut.init(controllerContext);

		try {
			// WHEN
			sut.handle(projectPath, emptyProjectQuery, newProjectBody);
			Assert.fail();
		} catch (IllegalStateException e) {
			// THEN
			Assert.assertEquals(e.getMessage(),
					"upon POST repository for type=projects must return created resource, not allowed to return null");
		}
	}


	@Test
	public void onPostingReadOnlyFieldReturnBadRequestWithFailBehavior() throws Exception {
		// GIVEN
		Document requestDocument = new Document();
		Resource data = createTask();
		data.getAttributes().put("readOnlyValue", objectMapper.readTree("\"newValue\""));
		requestDocument.setData(Nullable.of(data));

		JsonPath path = pathBuilder.build("/tasks");

		Mockito.when(propertiesProvider.getProperty(Mockito.eq(CrnkProperties.RESOURCE_FIELD_IMMUTABLE_WRITE_BEHAVIOR)))
				.thenReturn(ResourceFieldImmutableWriteBehavior.FAIL.toString());

		ResourcePostController sut = new ResourcePostController();
		sut.init(controllerContext);

		// WHEN
		try {
			sut.handle(path, emptyTaskQuery, requestDocument);
			Assert.fail("should not be allowed to update read-only field");
		} catch (ForbiddenException e) {
			Assert.assertEquals("field 'readOnlyValue' cannot be modified", e.getMessage());
		}
	}

	@Test
	public void onPostingReadOnlyFieldShouldIgnoreWithIgnoreBehavior() throws Exception {
		// GIVEN
		Document requestDocument = new Document();
		Resource data = createTask();
		data.getAttributes().put("readOnlyValue", objectMapper.readTree("\"newValue\""));
		requestDocument.setData(Nullable.of(data));

		JsonPath path = pathBuilder.build("/tasks");
		PropertiesProvider propertiesProvider = new PropertiesProvider() {

			@Override
			public String getProperty(String key) {
				if (CrnkProperties.RESOURCE_FIELD_IMMUTABLE_WRITE_BEHAVIOR.equals(key)) {
					return ResourceFieldImmutableWriteBehavior.IGNORE.toString();
				}
				return null;
			}
		};
		ResourcePostController sut = new ResourcePostController();
		sut.init(controllerContext);

		// WHEN
		Response response = sut.handle(path, emptyTaskQuery, requestDocument);
		String persistedValue = response.getDocument().getSingleData().get().getAttributes().get("readOnlyValue").asText();
		Assert.assertEquals("someReadOnlyValue", persistedValue);
	}

	@Test
	public void onNewResourcesAndRelationshipsShouldPersistThoseData() throws Exception {
		// GIVEN
		Document newProjectBody = new Document();
		Resource data = createProject();
		newProjectBody.setData(Nullable.of(data));
		data.setType("projects");

		JsonPath projectPath = pathBuilder.build("/projects");
		ResourcePostController sut = new ResourcePostController();
		sut.init(controllerContext);

		// WHEN
		Response projectResponse = sut.handle(projectPath, emptyProjectQuery, newProjectBody);

		// THEN
		assertThat(projectResponse.getDocument().getSingleData().get().getType()).isEqualTo("projects");
		assertThat(projectResponse.getDocument().getSingleData().get().getId()).isNotNull();
		assertThat(projectResponse.getDocument().getSingleData().get().getAttributes().get("name").asText())
				.isEqualTo("sample project");
		Long projectId = Long.parseLong(projectResponse.getDocument().getSingleData().get().getId());
		Mockito.verify(modificationFilter, Mockito.times(1))
				.modifyAttribute(Mockito.any(), Mockito.any(ResourceField.class), Mockito.eq("name"),
						Mockito.eq("sample project"));

		/* ------- */

		// GIVEN
		Document newUserBody = new Document();
		data = new Resource();
		newUserBody.setData(Nullable.of(data));
		data.setType("users");
		data.setAttribute("name", objectMapper.readTree("\"some user\""));
		List<ResourceIdentifier> projectIds = Collections.singletonList(new ResourceIdentifier(projectId.toString(),
				"projects"));
		data.getRelationships().put("assignedProjects", new Relationship(projectIds));

		JsonPath taskPath = pathBuilder.build("/users");

		// WHEN
		Response taskResponse = sut.handle(taskPath, emptyUserQuery, newUserBody);

		// THEN
		Resource task = taskResponse.getDocument().getSingleData().get();
		assertThat(task.getType()).isEqualTo("users");
		Long userId = Long.parseLong(task.getId());
		assertThat(userId).isNotNull();
		assertThat(task.getAttributes().get("name").asText()).isEqualTo("some user");
		assertThat(task.getRelationships().get("assignedProjects").getCollectionData().get()).hasSize(1);
		assertThat(
				task.getRelationships().get("assignedProjects").getCollectionData().get().get(0).getId())
				.isEqualTo(projectId.toString());
		Mockito.verify(modificationFilter, Mockito.times(1))
				.modifyManyRelationship(Mockito.any(), Mockito.any(ResourceField.class),
						Mockito.eq(ResourceRelationshipModificationType.SET), Mockito.eq(projectIds));
	}

	@Test
	public void onNewResourcesAndRelationshipsWithInvalidNameShouldReturnBadRequest() throws Exception {
		// GIVEN
		Document newProjectBody = new Document();
		Resource data = createProject();
		newProjectBody.setData(Nullable.of(data));
		data.setType("projects");

		JsonPath projectPath = pathBuilder.build("/projects");
		ResourcePostController sut = new ResourcePostController();
		sut.init(controllerContext);

		// WHEN
		Response projectResponse = sut.handle(projectPath, emptyProjectQuery, newProjectBody);

		// THEN
		assertThat(projectResponse.getDocument().getSingleData().get().getType()).isEqualTo("projects");
		assertThat(projectResponse.getDocument().getSingleData().get().getId()).isNotNull();
		assertThat(projectResponse.getDocument().getSingleData().get().getAttributes().get("name").asText())
				.isEqualTo("sample project");
		Long projectId = Long.parseLong(projectResponse.getDocument().getSingleData().get().getId());
		Mockito.verify(modificationFilter, Mockito.times(1))
				.modifyAttribute(Mockito.any(), Mockito.any(ResourceField.class), Mockito.eq("name"),
						Mockito.eq("sample project"));

		/* ------- */

		// GIVEN
		Document newUserBody = new Document();
		data = new Resource();
		newUserBody.setData(Nullable.of(data));
		data.setType("users");
		data.setAttribute("name", objectMapper.readTree("\"some user\""));
		// give a bad resource name inside the relationship
		List<ResourceIdentifier> projectIds = Collections.singletonList(new ResourceIdentifier(projectId.toString(),
				"notAResource"));

		data.getRelationships().put("assignedProjects", new Relationship(projectIds));

		JsonPath taskPath = pathBuilder.build("/users");

		// WHEN
		try {
			sut.handle(taskPath, emptyUserQuery, newUserBody);
			Assert.fail("should not be allowed to create a relationship with an invalid resource");
		} catch (BadRequestException e) {
			Assert.assertEquals("Invalid resource type: notAResource", e.getMessage());
		}
	}

	@Test
	public void onUpdatedLazyRelationshipDataShouldReturnThatData() {
		// GIVEN
		Document newTaskBody = new Document();
		Resource data = createTask();
		newTaskBody.setData(Nullable.of(data));
		data.setType("tasks");

		JsonPath taskPath = pathBuilder.build("/tasks");
		ResourcePostController sut = new ResourcePostController();
		sut.init(controllerContext);

		// WHEN
		Response taskResponse = sut.handle(taskPath, emptyTaskQuery, newTaskBody);

		// THEN
		assertThat(taskResponse.getDocument().getSingleData().get().getType()).isEqualTo("tasks");
		assertThat(taskResponse.getDocument().getSingleData().get().getId()).isNotNull();
		assertThat(taskResponse.getDocument().getSingleData().get().getAttributes().get("name").asText())
				.isEqualTo("sample task");
		Long taskId = Long.parseLong(taskResponse.getDocument().getSingleData().get().getId());

		/* ------- */

		// GIVEN

		Document newProjectBody = new Document();
		data = createProject();
		data.setType("projects");
		List<ResourceIdentifier> taskIds = Collections.singletonList(new ResourceIdentifier(taskId.toString(), "tasks"));
		data.getRelationships().put("tasks", new Relationship(taskIds));
		newProjectBody.setData(Nullable.of(data));

		JsonPath projectsPath = pathBuilder.build("/projects");

		// WHEN
		Response projectsResponse = sut.handle(projectsPath, emptyProjectQuery, newProjectBody);

		// THEN
		assertThat(projectsResponse.getDocument().getSingleData().get().getType()).isEqualTo("projects");
		Long userId = Long.parseLong(projectsResponse.getDocument().getSingleData().get().getId());
		assertThat(userId).isNotNull();
		assertThat(projectsResponse.getDocument().getSingleData().get().getAttributes().get("name").asText())
				.isEqualTo("sample project");

		assertThat(projectsResponse.getDocument().getSingleData().get().getRelationships().get("tasks").getCollectionData()
				.get())
				.hasSize(1);
		assertThat(projectsResponse.getDocument().getSingleData().get().getRelationships().get("tasks").getCollectionData().get()
				.get(0).getId()).isEqualTo(taskId.toString());

		Mockito.verify(modificationFilter, Mockito.times(1))
				.modifyManyRelationship(Mockito.any(), Mockito.any(ResourceField.class),
						Mockito.eq(ResourceRelationshipModificationType.SET), Mockito.eq(taskIds));
	}


	@Test
	public void onUnchangedLazyRelationshipDataShouldNotReturnThatData() {
		ResourcePostController sut = new ResourcePostController();
		sut.init(controllerContext);
		Document newProjectBody = new Document();
		Resource data = createProject();
		data.setType("projects");
		newProjectBody.setData(Nullable.of(data));

		JsonPath projectsPath = pathBuilder.build("/projects");

		// WHEN
		Response projectsResponse = sut.handle(projectsPath, emptyProjectQuery, newProjectBody);

		// THEN
		assertThat(projectsResponse.getDocument().getSingleData().get().getType()).isEqualTo("projects");
		Long userId = Long.parseLong(projectsResponse.getDocument().getSingleData().get().getId());
		assertThat(userId).isNotNull();
		assertThat(projectsResponse.getDocument().getSingleData().get().getAttributes().get("name").asText())
				.isEqualTo("sample project");
		assertThat(projectsResponse.getDocument().getSingleData().get().getRelationships().get("tasks").getData().isPresent())
				.isFalse();
	}

	@Test
	@Ignore // TODO
	public void onNewInheritedResourceShouldPersistThisResource() throws Exception {
		// GIVEN
		Document newMemorandumBody = new Document();
		Resource data = new Resource();
		newMemorandumBody.setData(Nullable.of(data));
		data.setType("memoranda");
		data.setAttribute("title", objectMapper.readTree("\"sample title\""));
		data.setAttribute("body", objectMapper.readTree("\"sample body\""));

		JsonPath projectPath = pathBuilder.build("/documents");
		ResourcePostController sut = new ResourcePostController();
		sut.init(controllerContext);

		// WHEN
		Response memorandumResponse = sut.handle(projectPath, emptyMemorandumQuery, newMemorandumBody);

		// THEN
		assertThat(memorandumResponse.getDocument().getSingleData().get().getType()).isEqualTo("memoranda");
		Resource persistedMemorandum = memorandumResponse.getDocument().getSingleData().get();
		assertThat(persistedMemorandum.getId()).isNotNull();
		assertThat(persistedMemorandum.getAttributes().get("title").asText()).isEqualTo("sample title");
		assertThat(persistedMemorandum.getAttributes().get("body").asText()).isEqualTo("sample body");
	}


	@Test
	@Ignore // TODO support inhertiance
	public void onResourceWithCustomNamesShouldSaveParametersCorrectly() {
		// GIVEN - creating sample project id
		Document newProjectBody = new Document();
		Resource data = createProject();
		newProjectBody.setData(Nullable.of(data));

		JsonPath projectPath = pathBuilder.build("/projects");
		ResourcePostController sut = new ResourcePostController();
		sut.init(controllerContext);

		// WHEN
		Response projectResponse = sut.handle(projectPath, emptyProjectQuery, newProjectBody);

		// THEN
		assertThat(projectResponse.getDocument().getSingleData().get().getType()).isEqualTo("projects");
		assertThat(projectResponse.getDocument().getSingleData().get().getId()).isNotNull();
		assertThat(projectResponse.getDocument().getSingleData().get().getAttributes().get("name").asText())
				.isEqualTo("sample project");
		Long projectId = Long.parseLong(projectResponse.getDocument().getSingleData().get().getId());

		/* ------- */

		// GIVEN
		Document pojoBody = new Document();
		Resource pojoData = new Resource();
		pojoBody.setData(Nullable.of(pojoData));
		pojoData.setType("pojo");
		JsonNode put = objectMapper.createObjectNode().put("value", "hello");
		pojoData.setAttribute("other-pojo", put);
		pojoData.getRelationships()
				.put("some-project", new Relationship(new ResourceIdentifier(Long.toString(projectId), "projects")));
		pojoData.getRelationships().put("some-projects",
				new Relationship(Arrays.asList(new ResourceIdentifier(Long.toString(projectId), "projects"))));

		JsonPath pojoPath = pathBuilder.build("/pojo");

		// WHEN
		QuerySpec querySpec = new QuerySpec(Pojo.class);
		querySpec.includeRelation(PathSpec.of("projects"));
		Response pojoResponse = sut.handle(pojoPath, container.toQueryAdapter(querySpec), pojoBody);

		// THEN
		assertThat(pojoResponse.getDocument().getSingleData().get().getType()).isEqualTo("pojo");
		Resource persistedPojo = pojoResponse.getDocument().getSingleData().get();
		assertThat(persistedPojo.getId()).isNotNull();
		assertThat(persistedPojo.getAttributes().get("other-pojo").get("value").asText()).isEqualTo("hello");
		assertThat(persistedPojo.getRelationships().get("some-project").getSingleData().get()).isNotNull();
		assertThat(persistedPojo.getRelationships().get("some-project").getSingleData().get().getId())
				.isEqualTo(projectId.toString());
		Relationship persistedProjectsRelationship = persistedPojo.getRelationships().get("some-projects");
		assertThat(persistedProjectsRelationship).isNotNull();

		// check lazy loaded relation
		ResourceRepositoryV2 repo = resourceRegistry.getEntry(Pojo.class).getResourceRepositoryFacade();
		Pojo pojo = (Pojo) repo.findOne(null, null);
		assertThat(pojo.getProjects()).hasSize(1);
		assertThat(pojo.getProjects().get(0).getId()).isEqualTo(projectId);
	}

	@Test
	public void onResourceWithInvalidRelationshipNameShouldThrowException() throws Exception {
		// GIVEN - creating sample project id
		Document newProjectBody = new Document();
		Resource data = createProject();
		newProjectBody.setData(Nullable.of(data));

		JsonPath projectPath = pathBuilder.build("/projects");
		ResourcePostController sut = new ResourcePostController();
		sut.init(controllerContext);

		// WHEN
		Response projectResponse = sut.handle(projectPath, emptyTaskQuery, newProjectBody);

		// THEN
		assertThat(projectResponse.getDocument().getSingleData().get().getType()).isEqualTo("projects");
		assertThat(projectResponse.getDocument().getSingleData().get().getId()).isNotNull();
		assertThat(projectResponse.getDocument().getSingleData().get().getAttributes().get("name").asText())
				.isEqualTo("sample project");
		Long projectId = Long.parseLong(projectResponse.getDocument().getSingleData().get().getId());

		/* ------- */

		// GIVEN
		Document pojoBody = new Document();
		Resource pojoData = new Resource();
		pojoBody.setData(Nullable.of(pojoData));
		pojoData.setType("pojo");
		JsonNode put = objectMapper.createObjectNode().put("value", "hello");
		pojoData.setAttribute("other-pojo", objectMapper.readTree("null"));
		String invalidRelationshipName = "invalid-relationship";
		pojoData.getRelationships()
				.put(invalidRelationshipName, new Relationship(new ResourceIdentifier(Long.toString(projectId), "projects")));

		JsonPath pojoPath = pathBuilder.build("/pojo");

		// THEN
		expectedException.expect(ResourceException.class);
		expectedException.expectMessage(String.format("Invalid relationship name: %s", invalidRelationshipName));

		// WHEN
		sut.handle(pojoPath, container.toQueryAdapter(new QuerySpec(Pojo.class)), pojoBody);
	}
}
