package io.crnk.core.engine.internal.dispatcher.controller.resource;

import io.crnk.core.boot.CrnkProperties;
import io.crnk.core.engine.dispatcher.Response;
import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.document.Relationship;
import io.crnk.core.engine.document.Resource;
import io.crnk.core.engine.document.ResourceIdentifier;
import io.crnk.core.engine.filter.ResourceModificationFilter;
import io.crnk.core.engine.filter.ResourceRelationshipModificationType;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.internal.dispatcher.controller.BaseControllerTest;
import io.crnk.core.engine.internal.dispatcher.controller.ResourceGet;
import io.crnk.core.engine.internal.dispatcher.controller.ResourcePatch;
import io.crnk.core.engine.internal.dispatcher.controller.ResourcePost;
import io.crnk.core.engine.internal.dispatcher.path.JsonPath;
import io.crnk.core.engine.internal.dispatcher.path.ResourcePath;
import io.crnk.core.engine.internal.repository.ResourceRepositoryAdapter;
import io.crnk.core.engine.properties.PropertiesProvider;
import io.crnk.core.engine.properties.ResourceFieldImmutableWriteBehavior;
import io.crnk.core.exception.BadRequestException;
import io.crnk.core.exception.CrnkException;
import io.crnk.core.exception.ForbiddenException;
import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.core.mock.models.Task;
import io.crnk.core.repository.response.JsonApiResponse;
import io.crnk.core.utils.Nullable;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class ResourcePatchTest extends BaseControllerTest {

	private static final String REQUEST_TYPE = "PATCH";

	@Test
	public void onGivenRequestCollectionGetShouldDenyIt() {
		// GIVEN
		JsonPath jsonPath = pathBuilder.build("/tasks/");
		ResourcePatch sut = new ResourcePatch(resourceRegistry, PROPERTIES_PROVIDER, typeParser, objectMapper, documentMapper, modificationFilters);

		// WHEN
		boolean result = sut.isAcceptable(jsonPath, REQUEST_TYPE);

		// THEN
		Assert.assertEquals(result, false);
	}

	@Test
	public void onGivenRequestResourceGetShouldAcceptIt() {
		// GIVEN
		JsonPath jsonPath = pathBuilder.build("/tasks/1");
		ResourcePatch sut = new ResourcePatch(resourceRegistry, PROPERTIES_PROVIDER, typeParser, objectMapper, documentMapper, modificationFilters);

		// WHEN
		boolean result = sut.isAcceptable(jsonPath, REQUEST_TYPE);

		// THEN
		Assert.assertEquals(result, true);
	}

	@Test
	public void onNoBodyResourceShouldThrowException() throws Exception {
		// GIVEN
		ResourcePost sut = new ResourcePost(resourceRegistry, PROPERTIES_PROVIDER, typeParser, objectMapper, documentMapper, modificationFilters);

		// THEN
		expectedException.expect(RuntimeException.class);

		// WHEN
		sut.handle(new ResourcePath("fridges"), emptyProjectQuery, null, null);
	}

	@Test
	public void onGivenRequestResourceGetShouldHandleIt() throws Exception {
		// GIVEN
		Document newTaskBody = new Document();
		Resource data = createTask();
		newTaskBody.setData(Nullable.of((Object) data));

		JsonPath taskPath = pathBuilder.build("/tasks");

		// WHEN
		ResourcePost resourcePost =
				new ResourcePost(resourceRegistry, PROPERTIES_PROVIDER, typeParser, objectMapper, documentMapper, modificationFilters);
		Response taskResponse = resourcePost.handle(taskPath, emptyTaskQuery, null, newTaskBody);
		assertThat(taskResponse.getDocument().getSingleData().get().getType()).isEqualTo("tasks");
		Long taskId = Long.parseLong(taskResponse.getDocument().getSingleData().get().getId());
		assertThat(taskId).isNotNull();

		// GIVEN
		Document taskPatch = new Document();
		data = createTask();
		taskPatch.setData(Nullable.of((Object) data));
		data.setAttribute("name", objectMapper.readTree("\"task updated\""));
		JsonPath jsonPath = pathBuilder.build("/tasks/" + taskId);
		ResourcePatch sut = new ResourcePatch(resourceRegistry, PROPERTIES_PROVIDER, typeParser, objectMapper, documentMapper, modificationFilters);

		// WHEN
		Response response = sut.handle(jsonPath, emptyTaskQuery, null, taskPatch);

		// THEN
		Assert.assertNotNull(response);
		assertThat(response.getDocument().getSingleData().get().getType()).isEqualTo("tasks");
		assertThat(response.getDocument().getSingleData().get().getAttributes().get("name").asText()).isEqualTo("task updated");

		Mockito.verify(modificationFilter, Mockito.times(1)).modifyAttribute(Mockito.any(), Mockito.any(ResourceField.class), Mockito.eq("name"), Mockito.eq("task updated"));

	}

	@Test(expected = ResourceNotFoundException.class)
	public void onPatchNonExistingResourceThrowException() throws Exception {
		// GIVEN
		Document newTaskBody = new Document();
		Resource data = createTask();
		newTaskBody.setData(Nullable.of((Object) data));

		Document taskPatch = new Document();
		data = createTask();
		taskPatch.setData(Nullable.of((Object) data));
		data.setAttribute("name", objectMapper.readTree("\"task updated\""));
		JsonPath jsonPath = pathBuilder.build("/tasks/1234567");
		ResourcePatch sut = new ResourcePatch(resourceRegistry, PROPERTIES_PROVIDER, typeParser, objectMapper, documentMapper, modificationFilters);

		// WHEN
		sut.handle(jsonPath, emptyTaskQuery, null, taskPatch);
	}

	@Test
	public void onPatchingReadOnlyFieldReturnBadRequestWithFailBehavior() throws Exception {
		// GIVEN
		Document requestDocument = new Document();
		Resource data = createTask();
		requestDocument.setData(Nullable.of((Object) data));

		JsonPath postPath = pathBuilder.build("/tasks");
		ResourcePost post = new ResourcePost(resourceRegistry, PROPERTIES_PROVIDER, typeParser, objectMapper, documentMapper, modificationFilters);
		post.handle(postPath, emptyTaskQuery, null, requestDocument);

		PropertiesProvider propertiesProvider = new PropertiesProvider() {

			@Override
			public String getProperty(String key) {
				if (CrnkProperties.RESOURCE_FIELD_IMMUTABLE_WRITE_BEHAVIOR.equals(key)) {
					return ResourceFieldImmutableWriteBehavior.FAIL.toString();
				}
				return null;
			}
		};

		ResourcePatch sut = new ResourcePatch(resourceRegistry, propertiesProvider, typeParser, objectMapper, documentMapper, modificationFilters);
		data.getAttributes().put("readOnlyValue", objectMapper.readTree("\"newValue\""));

		// WHEN
		try {
			JsonPath patchPath = pathBuilder.build("/tasks/" + data.getId());
			sut.handle(patchPath, emptyTaskQuery, null, requestDocument);
			Assert.fail("should not be allowed to update read-only field");
		} catch (ForbiddenException e) {
			Assert.assertEquals("field 'readOnlyValue' cannot be modified", e.getMessage());
		}
	}

	@Test
	public void onGivenRequestResourceShouldThrowException() throws Exception {
		// GIVEN
		Document newTaskBody = new Document();
		Resource data = createTask();
		newTaskBody.setData(Nullable.of((Object) data));
		data.setType("tasks");

		JsonPath taskPath = pathBuilder.build("/tasks");

		// WHEN
		ResourcePost resourcePost =
				new ResourcePost(resourceRegistry, PROPERTIES_PROVIDER, typeParser, objectMapper, documentMapper, modificationFilters);
		Response taskResponse = resourcePost.handle(taskPath, emptyTaskQuery, null, newTaskBody);
		assertThat(taskResponse.getDocument().getSingleData().get().getType()).isEqualTo("tasks");
		Long taskId = Long.parseLong(taskResponse.getDocument().getSingleData().get().getId());
		assertThat(taskId).isNotNull();

		// GIVEN
		Document taskPatch = new Document();
		data = new Resource();
		taskPatch.setData(Nullable.of((Object) data));
		data.setType("WRONG_AND_MISSING_TYPE");
		data.setAttribute("name", objectMapper.readTree("\"task updated\""));
		JsonPath jsonPath = pathBuilder.build("/tasks/" + taskId);
		ResourcePatch sut = new ResourcePatch(resourceRegistry, PROPERTIES_PROVIDER, typeParser, objectMapper, documentMapper, modificationFilters);

		// WHEN
		Response response = null;
		try {
			response = sut.handle(jsonPath, emptyTaskQuery, null, taskPatch);
			Assert.fail("Should have recieved exception.");
		} catch (CrnkException rbe) {
			// Got correct exception
		} catch (Error ex) {
			Assert.fail("Got bad exception: " + ex);
		}
	}

	@Test
	@org.junit.Ignore // TODO inhertiance/resourceregistry
	public void onInheritedResourceShouldUpdateInheritedResource() throws Exception {
		// GIVEN
		Document memorandumBody = new Document();
		Resource data = new Resource();
		memorandumBody.setData(Nullable.of((Object) data));
		data.setType("memoranda");
		data.setAttribute("title", objectMapper.readTree("\"some title\""));
		data.setAttribute("body", objectMapper.readTree("\"sample body\""));

		JsonPath documentsPath = pathBuilder.build("/documents");

		ResourcePost resourcePost =
				new ResourcePost(resourceRegistry, PROPERTIES_PROVIDER, typeParser, objectMapper, documentMapper, modificationFilters);

		// WHEN
		Response taskResponse = resourcePost.handle(documentsPath, emptyMemorandumQuery, null, memorandumBody);

		// THEN
		assertThat(taskResponse.getDocument().getSingleData().get().getType()).isEqualTo("memoranda");
		Long memorandumId = Long.parseLong(taskResponse.getDocument().getSingleData().get().getId());
		assertThat(memorandumId).isNotNull();

		// --------------------------

		// GIVEN
		memorandumBody = new Document();
		data = new Resource();
		memorandumBody.setData(Nullable.of((Object) data));
		data.setType("memoranda");
		data.setAttribute("title", objectMapper.readTree("\"new title\""));
		data.setAttribute("body", objectMapper.readTree("\"new body\""));
		JsonPath documentPath = pathBuilder.build("/documents/" + memorandumId);
		ResourcePatch sut = new ResourcePatch(resourceRegistry, PROPERTIES_PROVIDER, typeParser, objectMapper, documentMapper, modificationFilters);

		// WHEN
		Response memorandumResponse = sut.handle(documentPath, emptyMemorandumQuery, null, memorandumBody);

		// THEN
		assertThat(memorandumResponse.getDocument().getSingleData().get().getType()).isEqualTo("memoranda");
		Resource persistedMemorandum = memorandumResponse.getDocument().getSingleData().get();
		assertThat(persistedMemorandum.getId()).isNotNull();
		assertThat(persistedMemorandum.getAttributes().get("title").asText()).isEqualTo("new title");
		assertThat(persistedMemorandum.getAttributes().get("body").asText()).isEqualTo("new body");
	}

	@Test
	public void onResourceRelationshipNullifiedShouldSaveIt() throws Exception {
		// GIVEN
		Document newTaskBody = new Document();
		Resource data = createTask();
		newTaskBody.setData(Nullable.of((Object) data));

		JsonPath taskPath = pathBuilder.build("/tasks");

		// WHEN
		ResourcePost resourcePost =
				new ResourcePost(resourceRegistry, PROPERTIES_PROVIDER, typeParser, objectMapper, documentMapper, modificationFilters);
		Response taskResponse = resourcePost.handle(taskPath, emptyTaskQuery, null, newTaskBody);
		assertThat(taskResponse.getDocument().getSingleData().get().getType()).isEqualTo("tasks");
		Long taskId = Long.parseLong(taskResponse.getDocument().getSingleData().get().getId());
		assertThat(taskId).isNotNull();

		// GIVEN
		Document taskPatch = new Document();
		data = createTask();
		data.setAttribute("name", objectMapper.readTree("\"task updated\""));
		data.getRelationships().put("project", null);
		taskPatch.setData(Nullable.of((Object) data));
		JsonPath jsonPath = pathBuilder.build("/tasks/" + taskId);
		ResourcePatch sut = new ResourcePatch(resourceRegistry, PROPERTIES_PROVIDER, typeParser, objectMapper, documentMapper, modificationFilters);

		// WHEN
		Response response = sut.handle(jsonPath, emptyTaskQuery, null, taskPatch);

		// THEN
		Assert.assertNotNull(response);
		assertThat(response.getDocument().getSingleData().get().getType()).isEqualTo("tasks");
		assertThat(response.getDocument().getSingleData().get().getAttributes().get("name").asText()).isEqualTo("task updated");
		assertThat(response.getDocument().getSingleData().get().getRelationships().get("project").getData().get()).isNull();
	}

	@Test
	public void onUpdatedLazyRelationshipDataShouldReturnThatData() throws Exception {
		// GIVEN
		Document newTaskBody = new Document();
		Resource data = createTask();
		newTaskBody.setData(Nullable.of((Object) data));
		data.setType("tasks");

		JsonPath taskPath = pathBuilder.build("/tasks");
		ResourcePost post = new ResourcePost(resourceRegistry, PROPERTIES_PROVIDER, typeParser, objectMapper, documentMapper, modificationFilters);
		Response taskResponse = post.handle(taskPath, emptyTaskQuery, null, newTaskBody);
		Long taskId = Long.parseLong(taskResponse.getDocument().getSingleData().get().getId());

		Document newProjectBody = new Document();
		data = createProject();
		data.setType("projects");
		data.getRelationships()
				.put("tasks", new Relationship(Collections.singletonList(new ResourceIdentifier(taskId.toString(), "tasks"))));
		newProjectBody.setData(Nullable.of((Object) data));

		JsonPath projectsPath = pathBuilder.build("/projects");
		Response projectsResponse = post.handle(projectsPath, emptyProjectQuery, null, newProjectBody);
		assertThat(projectsResponse.getDocument().getSingleData().get().getRelationships().get("tasks").getCollectionData()
				.get())
				.hasSize(1);

		// update relationship and availability in response
		ResourcePatch patch = new ResourcePatch(resourceRegistry, PROPERTIES_PROVIDER, typeParser, objectMapper, documentMapper, modificationFilters);

		Nullable<Object> emptyRelation = Nullable.of((Object) new ArrayList<ResourceIdentifier>());
		data.getRelationships().get("tasks").setData(emptyRelation);
		projectsResponse =
				patch.handle(pathBuilder.build("/projects/2"), emptyProjectQuery, null, newProjectBody);
		assertThat(projectsResponse.getDocument().getSingleData().get().getType()).isEqualTo("projects");
		assertThat(projectsResponse.getDocument().getSingleData().get().getAttributes().get("name").asText())
				.isEqualTo("sample project");
		assertThat(projectsResponse.getDocument().getSingleData().get().getRelationships().get("tasks").getCollectionData()
				.get())
				.hasSize(0);

	}

	@Test
	public void patchNestedAttribute() throws Exception {
		// GIVEN
		Document newProjectBody = new Document();
		Resource data = createProject();
		data.getAttributes().remove("data");
		newProjectBody.setData(Nullable.of((Object) data));
		JsonPath taskPath = pathBuilder.build("/projects");

		// WHEN
		ResourcePost resourcePost =
				new ResourcePost(resourceRegistry, PROPERTIES_PROVIDER, typeParser, objectMapper, documentMapper, modificationFilters);
		Response projectResponse = resourcePost.handle(taskPath, emptyProjectQuery, null, newProjectBody);
		Resource savedProject = projectResponse.getDocument().getSingleData().get();
		assertThat(savedProject.getType()).isEqualTo("projects");
		assertThat(projectResponse.getDocument().getSingleData().get().getAttributes().get("data")).isNull();
		Long projectId = Long.parseLong(projectResponse.getDocument().getSingleData().get().getId());
		assertThat(projectId).isNotNull();

		// GIVEN
		data = new Resource();
		data.setType("projects");
		data.setId(savedProject.getId());
		data.setAttribute("data", objectMapper.readTree("{\"data\" : \"updated data\"}"));

		Document projectPatch = new Document();
		projectPatch.setData(Nullable.of((Object) data));
		JsonPath jsonPath = pathBuilder.build("/projects/" + projectId);
		ResourcePatch sut = new ResourcePatch(resourceRegistry, PROPERTIES_PROVIDER, typeParser, objectMapper, documentMapper, modificationFilters);

		// WHEN
		Response response = sut.handle(jsonPath, emptyProjectQuery, null, projectPatch);

		// THEN
		assertThat(response.getDocument().getSingleData().get().getAttributes().get("name").asText()).isEqualTo("sample "
				+ "project");
		assertThat(response.getDocument().getSingleData().get().getAttributes().get("data").get("data").asText())
				.isEqualTo("updated data");

	}

	@Test
	public void onUnchagedLazyRelationshipDataShouldNotReturnThatData() throws Exception {
		// GIVEN
		Document newTaskBody = new Document();
		Resource data = createTask();
		newTaskBody.setData(Nullable.of((Object) data));
		data.setType("tasks");

		JsonPath taskPath = pathBuilder.build("/tasks");
		ResourcePost post = new ResourcePost(resourceRegistry, PROPERTIES_PROVIDER, typeParser, objectMapper, documentMapper, modificationFilters);
		Response taskResponse = post.handle(taskPath, emptyTaskQuery, null, newTaskBody);
		Long taskId = Long.parseLong(taskResponse.getDocument().getSingleData().get().getId());

		Document newProjectBody = new Document();
		data = createProject();
		data.setType("projects");
		data.getRelationships()
				.put("tasks", new Relationship(Collections.singletonList(new ResourceIdentifier(taskId.toString(), "tasks"))));
		newProjectBody.setData(Nullable.of((Object) data));

		JsonPath projectsPath = pathBuilder.build("/projects");
		Response projectsResponse = post.handle(projectsPath, emptyProjectQuery, null, newProjectBody);
		assertThat(projectsResponse.getDocument().getSingleData().get().getRelationships().get("tasks").getCollectionData()
				.get())
				.hasSize(1);

		// update relationship and availability in response
		ResourcePatch patch = new ResourcePatch(resourceRegistry, PROPERTIES_PROVIDER, typeParser, objectMapper, documentMapper, modificationFilters);

		data.getRelationships().remove("tasks");
		data.getAttributes().put("name", objectMapper.readTree("\"updated project\""));
		projectsResponse =
				patch.handle(pathBuilder.build("/projects/2"), emptyProjectQuery, null, newProjectBody);
		assertThat(projectsResponse.getDocument().getSingleData().get().getType()).isEqualTo("projects");
		assertThat(projectsResponse.getDocument().getSingleData().get().getAttributes().get("name").asText())
				.isEqualTo("updated project");
		assertThat(projectsResponse.getDocument().getSingleData().get().getRelationships().get("tasks").getCollectionData()
				.isPresent()).isFalse();

	}

	@Test
	public void onGivenRequestResourcePatchShouldHandleMissingFields() throws Exception {

		JsonPath complexPojoPath = pathBuilder.build("/complexpojos/1");

		// WHEN
		ResourceGet resourceGet = new ResourceGet(resourceRegistry, typeParser, documentMapper);
		Response complexPojoResponse = resourceGet.handle(complexPojoPath, emptyComplexPojoQuery, null, null);
		assertThat(complexPojoResponse.getDocument().getSingleData().get().getType()).isEqualTo("complexpojos");
		Long complexPojoId = Long.parseLong(complexPojoResponse.getDocument().getSingleData().get().getId());
		assertThat(complexPojoId).isNotNull();
		assertThat(complexPojoResponse.getDocument().getSingleData().get().getAttributes().get("containedPojo").get
				("updateableProperty1").asText()).isEqualTo("value from repository mock");

		// GIVEN
		Document complexPojoPatch = new Document();
		Resource data = new Resource();
		complexPojoPatch.setData(Nullable.of((Object) data));
		data.setType("complexpojos");

		String rawContainedPatchData = "  {" + "    'updateableProperty1':'updated value'" + "  }";
		rawContainedPatchData = rawContainedPatchData.replaceAll("'", "\"");
		data.setAttribute("containedPojo", objectMapper.readTree(rawContainedPatchData));
		data.setAttribute("updateableProperty", objectMapper.readTree("\"wasNullBefore\""));

		JsonPath jsonPath = pathBuilder.build("/complexpojos/" + complexPojoId);
		ResourcePatch sut = new ResourcePatch(resourceRegistry, PROPERTIES_PROVIDER, typeParser, objectMapper, documentMapper, modificationFilters);

		// WHEN
		Response response = sut.handle(jsonPath, emptyComplexPojoQuery, null, complexPojoPatch);

		// THEN
		Assert.assertNotNull(response);
		assertThat(response.getDocument().getSingleData().get().getType()).isEqualTo("complexpojos");
		assertThat(response.getDocument().getSingleData().get().getAttributes().get("containedPojo").get("updateableProperty1")
				.asText()).isEqualTo("updated value");
		assertThat(response.getDocument().getSingleData().get().getAttributes().get("containedPojo").get("updateableProperty2")
				.asText()).isEqualTo("value from repository mock");
		assertThat(response.getDocument().getSingleData().get().getAttributes().get("updateableProperty").asText())
				.isEqualTo("wasNullBefore");
	}

	/*
	 * see github #122
	 */
	@Test
	public void omittedFieldsSettersAreNotCalled() throws Exception {
		// GIVEN
		ResourceRepositoryAdapter taskRepo = resourceRegistry.getEntry(Task.class).getResourceRepository(null);
		Task task = new Task();
		task.setName("Mary Joe");
		JsonApiResponse jsonApiResponse = taskRepo.create(task, emptyTaskQuery);
		task = (Task) (jsonApiResponse.getEntity());

		// GIVEN
		Document taskPatch = new Document();
		Resource data = new Resource();
		taskPatch.setData(Nullable.of((Object) data));
		data.setType("tasks");
		data.setAttribute("name", objectMapper.readTree("\"Mary Jane\""));
		JsonPath jsonPath = pathBuilder.build("/tasks/" + task.getId());
		ResourcePatch sut = new ResourcePatch(resourceRegistry, PROPERTIES_PROVIDER, typeParser, objectMapper, documentMapper, modificationFilters);

		// WHEN
		Response response = sut.handle(jsonPath, emptyTaskQuery, null, taskPatch);

		// THEN
		Assert.assertNotNull(response);
		assertThat(response.getDocument().getSingleData().get().getType()).isEqualTo("tasks");
		Resource updatedTask = response.getDocument().getSingleData().get();
		assertThat(updatedTask.getAttributes().get("name").asText()).isEqualTo("Mary Jane");
		assertThat(updatedTask.getId()).isEqualTo(task.getId().toString());
		assertThat(updatedTask.getAttributes().get("category")).isNull();
	}


	@Test
	public void omittedFieldsShouldBeIgnored() throws Exception {
		// GIVEN
		ResourceRepositoryAdapter taskRepo = resourceRegistry.getEntry(Task.class).getResourceRepository(null);
		Task task = new Task();
		task.setName("Mary Joe");
		JsonApiResponse jsonApiResponse = taskRepo.create(task, emptyTaskQuery);
		task = (Task) (jsonApiResponse.getEntity());

		// GIVEN
		Document taskPatch = new Document();
		Resource data = new Resource();
		taskPatch.setData(Nullable.of((Object) data));
		data.setType("tasks");
		data.setAttribute("category", objectMapper.readTree("\"TestCategory\""));
		JsonPath jsonPath = pathBuilder.build("/tasks/" + task.getId());
		ResourcePatch sut = new ResourcePatch(resourceRegistry, PROPERTIES_PROVIDER, typeParser, objectMapper, documentMapper,
				modificationFilters);

		// WHEN
		Response response = sut.handle(jsonPath, emptyTaskQuery, null, taskPatch);

		// THEN
		Assert.assertNotNull(response);
		assertThat(response.getDocument().getSingleData().get().getType()).isEqualTo("tasks");
		Resource updatedTask = response.getDocument().getSingleData().get();
		assertThat(updatedTask.getAttributes().get("name").asText()).isEqualTo("Mary Joe");
		assertThat(updatedTask.getAttributes().get("category").asText()).isEqualTo("TestCategory");
		assertThat(updatedTask.getId()).isEqualTo(task.getId().toString());
		Assert.assertEquals("Mary Joe", task.getName());
		Assert.assertEquals("TestCategory", task.getCategory());
	}

}
