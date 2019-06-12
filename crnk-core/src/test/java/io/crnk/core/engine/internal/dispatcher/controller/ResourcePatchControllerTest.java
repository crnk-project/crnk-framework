package io.crnk.core.engine.internal.dispatcher.controller;

import com.fasterxml.jackson.databind.JsonNode;
import io.crnk.core.boot.CrnkProperties;
import io.crnk.core.engine.dispatcher.Response;
import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.document.Relationship;
import io.crnk.core.engine.document.Resource;
import io.crnk.core.engine.document.ResourceIdentifier;
import io.crnk.core.engine.http.HttpStatus;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.internal.dispatcher.path.JsonPath;
import io.crnk.core.engine.internal.repository.ResourceRepositoryAdapter;
import io.crnk.core.engine.properties.ResourceFieldImmutableWriteBehavior;
import io.crnk.core.exception.CrnkException;
import io.crnk.core.exception.ForbiddenException;
import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.core.mock.models.Project;
import io.crnk.core.mock.models.Schedule;
import io.crnk.core.mock.models.Task;
import io.crnk.core.mock.repository.ProjectRepository;
import io.crnk.core.mock.repository.ScheduleRepository;
import io.crnk.core.mock.repository.ScheduleRepositoryImpl;
import io.crnk.core.mock.repository.TaskRepository;
import io.crnk.core.repository.response.JsonApiResponse;
import io.crnk.core.utils.Nullable;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ResourcePatchControllerTest extends ControllerTestBase {

    private static final String REQUEST_TYPE = "PATCH";

    @Test
    public void onGivenRequestCollectionGetShouldDenyIt() {
        // GIVEN
        JsonPath jsonPath = pathBuilder.build("/tasks/");
        ResourcePatchController sut = new ResourcePatchController();
        sut.init(controllerContext);

        // WHEN
        boolean result = sut.isAcceptable(jsonPath, REQUEST_TYPE);

        // THEN
        Assert.assertEquals(result, false);
    }

    @Test
    public void onGivenRequestResourceGetShouldAcceptIt() {
        // GIVEN
        JsonPath jsonPath = pathBuilder.build("/tasks/1");
        ResourcePatchController sut = new ResourcePatchController();
        sut.init(controllerContext);

        // WHEN
        boolean result = sut.isAcceptable(jsonPath, REQUEST_TYPE);

        // THEN
        Assert.assertEquals(result, true);
    }

    @Test
    public void onNoBodyResourceShouldThrowException() {
        // GIVEN
        ResourcePostController sut = new ResourcePostController();
        sut.init(controllerContext);

        // THEN
        expectedException.expect(RuntimeException.class);

        // WHEN
        JsonPath path = pathBuilder.build("/frides");
        sut.handle(path, emptyProjectQuery, null);
    }

    @Test
    public void onGivenRequestResourceGetShouldHandleIt() throws Exception {
        // GIVEN
        Document newTaskBody = new Document();
        Resource data = createTask();
        newTaskBody.setData(Nullable.of(data));

        JsonPath taskPath = pathBuilder.build("/tasks");

        // WHEN
        ResourcePostController resourcePost = new ResourcePostController();
        resourcePost.init(controllerContext);
        Response taskResponse = resourcePost.handle(taskPath, emptyTaskQuery, newTaskBody);
        assertThat(taskResponse.getDocument().getSingleData().get().getType()).isEqualTo("tasks");
        Long taskId = Long.parseLong(taskResponse.getDocument().getSingleData().get().getId());
        assertThat(taskId).isNotNull();

        // GIVEN
        Document taskPatch = new Document();
        data = createTask();
        taskPatch.setData(Nullable.of(data));
        data.setAttribute("name", objectMapper.readTree("\"task updated\""));
        JsonPath jsonPath = pathBuilder.build("/tasks/" + taskId);
        ResourcePatchController sut = new ResourcePatchController();
        sut.init(controllerContext);

        // WHEN
        Response response = sut.handle(jsonPath, emptyTaskQuery, taskPatch);

        // THEN
        Assert.assertNotNull(response);
        assertThat(response.getDocument().getSingleData().get().getType()).isEqualTo("tasks");
        assertThat(response.getDocument().getSingleData().get().getAttributes().get("name").asText()).isEqualTo("task updated");

        Mockito.verify(modificationFilter, Mockito.times(1))
                .modifyAttribute(Mockito.any(), Mockito.any(ResourceField.class), Mockito.eq("name"), Mockito.eq("task "
                        + "updated"));

    }

    @Test
    public void onRepositoryReturnNullStatusCode204MustBeReturned() {
        ProjectRepository projectRepository = new ProjectRepository();
        Project project = new Project();
        project.setId(ProjectRepository.RETURN_NULL_ON_CREATE_ID);
        project.setName("returns null on update");
        projectRepository.save(project);

        // GIVEN
        Document newProjectBody = new Document();
        Resource data = createProject();
        data.setId(Long.toString(ProjectRepository.RETURN_NULL_ON_CREATE_ID));
        newProjectBody.setData(Nullable.of(data));

        JsonPath projectPath = pathBuilder.build("/projects/" + ProjectRepository.RETURN_NULL_ON_CREATE_ID);
        ResourcePatchController sut = new ResourcePatchController();
        sut.init(controllerContext);

        Response response = sut.handle(projectPath, emptyProjectQuery, newProjectBody);
        Assert.assertEquals(HttpStatus.NO_CONTENT_204, response.getHttpStatus().intValue());
    }

    @Test(expected = ResourceNotFoundException.class)
    public void onPatchNonExistingResourceThrowException() throws Exception {
        // GIVEN
        Document newTaskBody = new Document();
        Resource data = createTask();
        newTaskBody.setData(Nullable.of(data));

        Document taskPatch = new Document();
        data = createTask();
        taskPatch.setData(Nullable.of(data));
        data.setAttribute("name", objectMapper.readTree("\"task updated\""));
        JsonPath jsonPath = pathBuilder.build("/tasks/1234567");
        ResourcePatchController sut = new ResourcePatchController();
        sut.init(controllerContext);

        // WHEN
        sut.handle(jsonPath, emptyTaskQuery, taskPatch);
    }

    @Test
    public void onPatchingReadOnlyFieldReturnBadRequestWithFailBehavior() throws Exception {
        // GIVEN
        Document requestDocument = new Document();
        Resource data = createTask();
        requestDocument.setData(Nullable.of(data));

        JsonPath postPath = pathBuilder.build("/tasks");
        ResourcePostController post = new ResourcePostController();
        post.init(controllerContext);
        post.handle(postPath, emptyTaskQuery, requestDocument);

        Mockito.when(propertiesProvider.getProperty(Mockito.eq(CrnkProperties.RESOURCE_FIELD_IMMUTABLE_WRITE_BEHAVIOR)))
                .thenReturn(ResourceFieldImmutableWriteBehavior.FAIL.toString());

        ResourcePatchController sut = new ResourcePatchController();
        sut.init(controllerContext);
        data.getAttributes().put("readOnlyValue", objectMapper.readTree("\"newValue\""));

        // WHEN
        try {
            JsonPath patchPath = pathBuilder.build("/tasks/" + data.getId());
            sut.handle(patchPath, emptyTaskQuery, requestDocument);
            Assert.fail("should not be allowed to update read-only field");
        } catch (ForbiddenException e) {
            Assert.assertEquals("field 'tasks.readOnlyValue' cannot be accessed for PATCH", e.getMessage());
        }
    }

    @Test
    public void onGivenRequestResourceShouldThrowException() throws Exception {
        // GIVEN
        Document newTaskBody = new Document();
        Resource data = createTask();
        newTaskBody.setData(Nullable.of(data));
        data.setType("tasks");

        JsonPath taskPath = pathBuilder.build("/tasks");

        // WHEN
        ResourcePostController resourcePost = new ResourcePostController();
        resourcePost.init(controllerContext);
        Response taskResponse = resourcePost.handle(taskPath, emptyTaskQuery, newTaskBody);
        assertThat(taskResponse.getDocument().getSingleData().get().getType()).isEqualTo("tasks");
        Long taskId = Long.parseLong(taskResponse.getDocument().getSingleData().get().getId());
        assertThat(taskId).isNotNull();

        // GIVEN
        Document taskPatch = new Document();
        data = new Resource();
        taskPatch.setData(Nullable.of(data));
        data.setType("WRONG_AND_MISSING_TYPE");
        data.setAttribute("name", objectMapper.readTree("\"task updated\""));
        JsonPath jsonPath = pathBuilder.build("/tasks/" + taskId);
        ResourcePatchController sut = new ResourcePatchController();
        sut.init(controllerContext);

        // WHEN
        Response response = null;
        try {
            response = sut.handle(jsonPath, emptyTaskQuery, taskPatch);
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
        memorandumBody.setData(Nullable.of(data));
        data.setType("memoranda");
        data.setAttribute("title", objectMapper.readTree("\"some title\""));
        data.setAttribute("body", objectMapper.readTree("\"sample body\""));

        JsonPath documentsPath = pathBuilder.build("/documents");

        ResourcePostController resourcePost = new ResourcePostController();
        resourcePost.init(controllerContext);

        // WHEN
        Response taskResponse = resourcePost.handle(documentsPath, emptyMemorandumQuery, memorandumBody);

        // THEN
        assertThat(taskResponse.getDocument().getSingleData().get().getType()).isEqualTo("memoranda");
        Long memorandumId = Long.parseLong(taskResponse.getDocument().getSingleData().get().getId());
        assertThat(memorandumId).isNotNull();

        // --------------------------

        // GIVEN
        memorandumBody = new Document();
        data = new Resource();
        memorandumBody.setData(Nullable.of(data));
        data.setType("memoranda");
        data.setAttribute("title", objectMapper.readTree("\"new title\""));
        data.setAttribute("body", objectMapper.readTree("\"new body\""));
        JsonPath documentPath = pathBuilder.build("/documents/" + memorandumId);
        ResourcePatchController sut = new ResourcePatchController();
        sut.init(controllerContext);

        // WHEN
        Response memorandumResponse = sut.handle(documentPath, emptyMemorandumQuery, memorandumBody);

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
        Project project = new Project();
        project.setName("test");

        Task task = new Task();
        task.setName("test");
        task.setProject(project);

        ProjectRepository projectRepository = new ProjectRepository();
        projectRepository.save(project);
        TaskRepository taskRepository = new TaskRepository();
        taskRepository.save(task);

        JsonPath taskPath = pathBuilder.build("/tasks/" + task.getId());

        // verify relationship available
        ResourceGetController resourceGet = new ResourceGetController();
        resourceGet.init(controllerContext);
        Response taskResponse = resourceGet.handle(taskPath, emptyTaskQuery, null);
        Resource createdTask = taskResponse.getDocument().getSingleData().get();
        assertThat(createdTask.getType()).isEqualTo("tasks");
        Long taskId = Long.parseLong(createdTask.getId());
        assertThat(taskId).isNotNull();
        Relationship createProjectRelation = createdTask.getRelationships().get("project");
        assertThat(createProjectRelation.getData().isPresent());
        assertThat(createProjectRelation.getData().get()).isNotNull();

        // set relationship to null
        Document taskPatch = new Document();
        Resource data = createTask();
        data.setId(taskId.toString());
        data.setAttribute("id", objectMapper.readTree("\"task updated\""));
        data.setAttribute("name", objectMapper.readTree("\"task updated\""));
        Relationship nulledRelationship = new Relationship();
        nulledRelationship.setData(Nullable.nullValue());
        data.getRelationships().put("project", nulledRelationship);
        taskPatch.setData(Nullable.of(data));
        JsonPath jsonPath = pathBuilder.build("/tasks/" + taskId);
        ResourcePatchController sut = new ResourcePatchController();
        sut.init(controllerContext);
        Response response = sut.handle(jsonPath, emptyTaskQuery, taskPatch);

        // verify attributes updated and relationship nulled
        Assert.assertNotNull(response);
        assertThat(response.getDocument().getSingleData().get().getType()).isEqualTo("tasks");
        assertThat(response.getDocument().getSingleData().get().getAttributes().get("name").asText()).isEqualTo("task updated");
        assertThat(response.getDocument().getSingleData().get().getRelationships().get("project").getData().get()).isNull();
    }


    @Test
    public void onResourceRelationshipIdNullifiedShouldSaveIt() throws Exception {
        // GIVEN
        Project project = new Project();
        project.setName("test");
        ProjectRepository projectRepository = new ProjectRepository();
        projectRepository.save(project);
        assertThat(project.getId()).isNotNull();

        Schedule schedule = new Schedule();
        schedule.setId(12L);
        schedule.setName("test");
        schedule.setProjectId(project.getId());
        ScheduleRepository scheduleRepository = new ScheduleRepositoryImpl();
        scheduleRepository.save(schedule);
        assertThat(schedule.getId()).isNotNull();

        JsonPath schedulePath = pathBuilder.build("/schedules/" + schedule.getId());

        // verify relationship available
        ResourceGetController resourceGet = new ResourceGetController();
        resourceGet.init(controllerContext);
        Response scheduleResponse = resourceGet.handle(schedulePath, emptyScheduleQuery, null);
        Resource createdTask = scheduleResponse.getDocument().getSingleData().get();
        assertThat(createdTask.getType()).isEqualTo("schedules");
        Long taskId = Long.parseLong(createdTask.getId());
        assertThat(taskId).isNotNull();
        Relationship createProjectRelation = createdTask.getRelationships().get("project");
        assertThat(createProjectRelation.getData().isPresent());
        assertThat(createProjectRelation.getData().get()).isNotNull();

        // set relationship to null
        Document schedulePatch = new Document();
        Resource data = new Resource();
        data.setType("schedules");
        data.setId(taskId.toString());
        data.setAttribute("name", objectMapper.readTree("\"schedule updated\""));
        Relationship nulledRelationship = new Relationship();
        nulledRelationship.setData(Nullable.nullValue());
        data.getRelationships().put("project", nulledRelationship);
        schedulePatch.setData(Nullable.of(data));
        ResourcePatchController sut = new ResourcePatchController();
        sut.init(controllerContext);
        Response response = sut.handle(schedulePath, emptyScheduleQuery, schedulePatch);

        // verify attributes updated and relationship nulled
        Assert.assertNotNull(response);
        Resource updatedResource = response.getDocument().getSingleData().get();
        assertThat(updatedResource.getType()).isEqualTo("schedules");
        assertThat(updatedResource.getAttributes().get("name").asText()).isEqualTo("schedule updated");
        assertThat(updatedResource.getRelationships().get("project").getData().get()).isNull();
    }

    @Test
    public void ignoreNonPatchableRelationship() throws Exception {
        Task task = new Task();
        task.setName("test");
        task.setStatusThingId(13L);
        TaskRepository taskRepository = new TaskRepository();
        taskRepository.save(task);

        JsonPath taskPath = pathBuilder.build("/tasks/" + task.getId());

        // try set relationship
        Document taskPatch = new Document();
        Resource data = new Resource();
        data.setType("tasks");
        data.setId(task.getId().toString());
        Relationship nulledRelationship = new Relationship();
        nulledRelationship.setData(Nullable.nullValue());
        data.setAttribute("name", objectMapper.readTree("\"task updated\""));
        data.getRelationships().put("statusThing", nulledRelationship);
        taskPatch.setData(Nullable.of(data));
        ResourcePatchController sut = new ResourcePatchController();
        sut.init(controllerContext);
        Response response = sut.handle(taskPath, emptyTaskQuery, taskPatch);

        // not relationship not patched since not not patchable due to @JsonApiField(patchable=false)
        Assert.assertNotNull(response);
        Resource savedTask = response.getDocument().getSingleData().get();
        assertThat(savedTask.getType()).isEqualTo("tasks");
        assertThat(savedTask.getAttributes().get("name").asText()).isEqualTo("task updated");
        Relationship savedRelationshipId = savedTask.getRelationships().get("statusThing");
        assertThat(savedRelationshipId.getData().get()).isEqualTo(new ResourceIdentifier("13", "things"));
    }

    @Test
    public void onUpdatedLazyRelationshipDataShouldReturnThatData() {
        // GIVEN
        Document newTaskBody = new Document();
        Resource data = createTask();
        newTaskBody.setData(Nullable.of(data));
        data.setType("tasks");

        JsonPath taskPath = pathBuilder.build("/tasks");
        ResourcePostController post = new ResourcePostController();
        post.init(controllerContext);
        Response taskResponse = post.handle(taskPath, emptyTaskQuery, newTaskBody);
        Long taskId = Long.parseLong(taskResponse.getDocument().getSingleData().get().getId());

        Document newProjectBody = new Document();
        data = createProject();
        data.setType("projects");
        data.getRelationships()
                .put("tasks", new Relationship(Collections.singletonList(new ResourceIdentifier(taskId.toString(), "tasks"))));
        newProjectBody.setData(Nullable.of(data));

        JsonPath projectsPath = pathBuilder.build("/projects");
        Response projectsResponse = post.handle(projectsPath, emptyProjectQuery, newProjectBody);
        assertThat(projectsResponse.getDocument().getSingleData().get().getRelationships().get("tasks").getCollectionData()
                .get())
                .hasSize(1);

        // update relationship and availability in response
        ResourcePatchController patch = new ResourcePatchController();
        patch.init(controllerContext);

        Nullable<Object> emptyRelation = Nullable.of(new ArrayList<ResourceIdentifier>());
        data.getRelationships().get("tasks").setData(emptyRelation);
        projectsResponse =
                patch.handle(pathBuilder.build("/projects/2"), emptyProjectQuery, newProjectBody);
        assertThat(projectsResponse.getDocument().getSingleData().get().getType()).isEqualTo("projects");
        assertThat(projectsResponse.getDocument().getSingleData().get().getAttributes().get("name").asText())
                .isEqualTo("sample project");
        assertThat(projectsResponse.getDocument().getSingleData().get().getRelationships().get("tasks").getCollectionData()
                .get())
                .hasSize(0);

    }

    @Test
    public void mergeEmptyListShouldSaveIt() throws Exception {
        // GIVEN
        Document newProjectBody = new Document();
        Resource data = createProject();
        newProjectBody.setData(Nullable.of(data));
        JsonPath taskPath = pathBuilder.build("/projects");

        // WHEN
        ResourcePostController resourcePost = new ResourcePostController();
        resourcePost.init(controllerContext);
        Response projectResponse = resourcePost.handle(taskPath, emptyProjectQuery, newProjectBody);
        Resource savedProject = projectResponse.getDocument().getSingleData().get();

        // GIVEN
        data = new Resource();
        data.setType("projects");
        data.setId(savedProject.getId());
        data.setAttribute("data", objectMapper.readTree("{\"keywords\" : []}"));

        Document projectPatch = new Document();
        projectPatch.setData(Nullable.of(data));
        JsonPath jsonPath = pathBuilder.build("/projects/" + savedProject.getId());
        ResourcePatchController sut = new ResourcePatchController();
        sut.init(controllerContext);

        // WHEN
        Response response = sut.handle(jsonPath, emptyProjectQuery, projectPatch);

        // THEN
        Map<String, JsonNode> patchedAttributes = response.getDocument().getSingleData().get().getAttributes();
        assertThat(patchedAttributes.get("data").get("keywords").size()).isEqualTo(0);
    }

    @Test
    public void mergeNonEmptyListShouldSaveIt() throws Exception {
        // GIVEN
        Document newProjectBody = new Document();
        Resource data = createProject();
        newProjectBody.setData(Nullable.of(data));
        JsonPath taskPath = pathBuilder.build("/projects");

        // WHEN
        ResourcePostController resourcePost = new ResourcePostController();
        resourcePost.init(controllerContext);
        Response projectResponse = resourcePost.handle(taskPath, emptyProjectQuery, newProjectBody);
        Resource savedProject = projectResponse.getDocument().getSingleData().get();

        // GIVEN
        data = new Resource();
        data.setType("projects");
        data.setId(savedProject.getId());
        data.setAttribute("data", objectMapper.readTree("{\"keywords\" : [\"test\"]}"));

        Document projectPatch = new Document();
        projectPatch.setData(Nullable.of(data));
        JsonPath jsonPath = pathBuilder.build("/projects/" + savedProject.getId());
        ResourcePatchController sut = new ResourcePatchController();
        sut.init(controllerContext);

        // WHEN
        Response response = sut.handle(jsonPath, emptyProjectQuery, projectPatch);

        // THEN
        Map<String, JsonNode> patchedAttributes = response.getDocument().getSingleData().get().getAttributes();
        assertThat(patchedAttributes.get("data").get("keywords").size()).isEqualTo(1);
    }

    @Test
    public void mergeNestedAttributeWithDefaultPatchStrategy() throws Exception {
        // GIVEN
        Document newProjectBody = new Document();
        Resource data = createProject();
        data.getAttributes().remove("data");
        newProjectBody.setData(Nullable.of(data));
        JsonPath taskPath = pathBuilder.build("/projects");

        // WHEN
        ResourcePostController resourcePost = new ResourcePostController();
        resourcePost.init(controllerContext);
        Response projectResponse = resourcePost.handle(taskPath, emptyProjectQuery, newProjectBody);
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
        projectPatch.setData(Nullable.of(data));
        JsonPath jsonPath = pathBuilder.build("/projects/" + projectId);
        ResourcePatchController sut = new ResourcePatchController();
        sut.init(controllerContext);

        // WHEN
        Response response = sut.handle(jsonPath, emptyProjectQuery, projectPatch);

        // THEN
        assertThat(response.getDocument().getSingleData().get().getAttributes().get("name").asText()).isEqualTo("sample "
                + "project");
        assertThat(response.getDocument().getSingleData().get().getAttributes().get("data").get("data").asText())
                .isEqualTo("updated data");

    }

    @Test
    public void mergeDeeplyNestedAttributeWithDefaultPatchStrategy() throws Exception {
        // GIVEN
        Document newProjectBody = new Document();
        Resource data = createProject();
        newProjectBody.setData(Nullable.of(data));
        JsonPath taskPath = pathBuilder.build("/projects");

        // WHEN
        ResourcePostController resourcePost = new ResourcePostController();
        resourcePost.init(controllerContext);
        Response projectResponse = resourcePost.handle(taskPath, emptyProjectQuery, newProjectBody);
        Resource savedProject = projectResponse.getDocument().getSingleData().get();
        assertThat(savedProject.getType()).isEqualTo("projects");
        Map<String, JsonNode> createdAttributes = projectResponse.getDocument().getSingleData().get().getAttributes();
        assertThat(createdAttributes.get("data")).isNotNull();
        assertThat(createdAttributes.get("data").get("status").get("qualityStatus").asText()).isEqualTo("ok");
        assertThat(createdAttributes.get("data").get("status").get("timelineStatus").asText()).isEqualTo("ok");
        Long projectId = Long.parseLong(projectResponse.getDocument().getSingleData().get().getId());
        assertThat(projectId).isNotNull();

        // GIVEN
        data = new Resource();
        data.setType("projects");
        data.setId(savedProject.getId());
        data.setAttribute("data", objectMapper.readTree("{\"status\": { \"qualityStatus\": \"great\"}}"));

        Document projectPatch = new Document();
        projectPatch.setData(Nullable.of(data));
        JsonPath jsonPath = pathBuilder.build("/projects/" + projectId);
        ResourcePatchController sut = new ResourcePatchController();
        sut.init(controllerContext);

        // WHEN
        Response response = sut.handle(jsonPath, emptyProjectQuery, projectPatch);

        // THEN
        Map<String, JsonNode> attributes = response.getDocument().getSingleData().get().getAttributes();
        assertThat(attributes.get("name").asText()).isEqualTo("sample project");
        assertThat(attributes.get("data").get("data").asText()).isEqualTo("asd");
        assertThat(attributes.get("data").get("status").get("qualityStatus").asText()).isEqualTo("great");
        assertThat(attributes.get("data").get("status").get("timelineStatus").asText()).isEqualTo("ok");

    }

    @Test
    public void replaceNestedAttributeWithCustomPatchStrategy() throws Exception {
        // GIVEN
        Document newProjectBody = new Document();
        Resource data = createProject(Long.toString(PROJECT_ID), "projects-patch-strategy");
        newProjectBody.setData(Nullable.of(data));
        JsonPath taskPath = pathBuilder.build("/projects-patch-strategy");

        // WHEN
        ResourcePostController resourcePost = new ResourcePostController();
        resourcePost.init(controllerContext);
        Response projectResponse = resourcePost.handle(taskPath, emptyProjectQuery, newProjectBody);
        Resource savedProject = projectResponse.getDocument().getSingleData().get();
        assertThat(savedProject.getType()).isEqualTo("projects-patch-strategy");
        assertThat(projectResponse.getDocument().getSingleData().get().getAttributes().get("data").get("data").asText()).isEqualTo("asd");
        Long projectId = Long.parseLong(projectResponse.getDocument().getSingleData().get().getId());
        assertThat(projectId).isNotNull();

        // GIVEN
        data = new Resource();
        data.setType("projects-patch-strategy");
        data.setId(savedProject.getId());
        data.setAttribute("data", objectMapper.readTree("{\"data\" : \"updated data\"}"));

        Document projectPatch = new Document();
        projectPatch.setData(Nullable.of(data));
        JsonPath jsonPath = pathBuilder.build(taskPath + "/" + projectId);
        ResourcePatchController sut = new ResourcePatchController();
        sut.init(controllerContext);

        // WHEN
        Response response = sut.handle(jsonPath, emptyProjectQuery, projectPatch);

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
        newTaskBody.setData(Nullable.of(data));
        data.setType("tasks");

        JsonPath taskPath = pathBuilder.build("/tasks");
        ResourcePostController post = new ResourcePostController();
        post.init(controllerContext);
        Response taskResponse = post.handle(taskPath, emptyTaskQuery, newTaskBody);
        Long taskId = Long.parseLong(taskResponse.getDocument().getSingleData().get().getId());

        Document newProjectBody = new Document();
        data = createProject();
        data.setType("projects");
        data.getRelationships()
                .put("tasks", new Relationship(Collections.singletonList(new ResourceIdentifier(taskId.toString(), "tasks"))));
        newProjectBody.setData(Nullable.of(data));

        JsonPath projectsPath = pathBuilder.build("/projects");
        Response projectsResponse = post.handle(projectsPath, emptyProjectQuery, newProjectBody);
        assertThat(projectsResponse.getDocument().getSingleData().get().getRelationships().get("tasks").getCollectionData()
                .get())
                .hasSize(1);

        // update relationship and availability in response
        ResourcePatchController patch = new ResourcePatchController();
        patch.init(controllerContext);

        data.getRelationships().remove("tasks");
        data.getAttributes().put("name", objectMapper.readTree("\"updated project\""));
        projectsResponse =
                patch.handle(pathBuilder.build("/projects/2"), emptyProjectQuery, newProjectBody);
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
        ResourceGetController resourceGet = new ResourceGetController();
        resourceGet.init(controllerContext);
        Response complexPojoResponse = resourceGet.handle(complexPojoPath, emptyComplexPojoQuery, null);
        assertThat(complexPojoResponse.getDocument().getSingleData().get().getType()).isEqualTo("complexpojos");
        Long complexPojoId = Long.parseLong(complexPojoResponse.getDocument().getSingleData().get().getId());
        assertThat(complexPojoId).isNotNull();
        assertThat(complexPojoResponse.getDocument().getSingleData().get().getAttributes().get("containedPojo").get
                ("updateableProperty1").asText()).isEqualTo("value from repository mock");

        // GIVEN
        Document complexPojoPatch = new Document();
        Resource data = new Resource();
        complexPojoPatch.setData(Nullable.of(data));
        data.setType("complexpojos");

        String rawContainedPatchData = "  {" + "    'updateableProperty1':'updated value'" + "  }";
        rawContainedPatchData = rawContainedPatchData.replaceAll("'", "\"");
        data.setAttribute("containedPojo", objectMapper.readTree(rawContainedPatchData));
        data.setAttribute("updateableProperty", objectMapper.readTree("\"wasNullBefore\""));

        JsonPath jsonPath = pathBuilder.build("/complexpojos/" + complexPojoId);
        ResourcePatchController sut = new ResourcePatchController();
        sut.init(controllerContext);

        // WHEN
        Response response = sut.handle(jsonPath, emptyComplexPojoQuery, complexPojoPatch);

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
        ResourceRepositoryAdapter taskRepo = resourceRegistry.getEntry(Task.class).getResourceRepository();
        Task task = new Task();
        task.setName("Mary Joe");
        JsonApiResponse jsonApiResponse = taskRepo.create(task, emptyTaskQuery).get();
        task = (Task) (jsonApiResponse.getEntity());

        // GIVEN
        Document taskPatch = new Document();
        Resource data = new Resource();
        taskPatch.setData(Nullable.of(data));
        data.setType("tasks");
        data.setAttribute("name", objectMapper.readTree("\"Mary Jane\""));
        JsonPath jsonPath = pathBuilder.build("/tasks/" + task.getId());
        ResourcePatchController sut = new ResourcePatchController();
        sut.init(controllerContext);

        // WHEN
        Response response = sut.handle(jsonPath, emptyTaskQuery, taskPatch);

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
        ResourceRepositoryAdapter taskRepo = resourceRegistry.getEntry(Task.class).getResourceRepository();
        Task task = new Task();
        task.setName("Mary Joe");
        JsonApiResponse jsonApiResponse = taskRepo.create(task, emptyTaskQuery).get();
        task = (Task) (jsonApiResponse.getEntity());

        // GIVEN
        Resource data = new Resource();
        data.setType("tasks");
        data.setAttribute("category", objectMapper.readTree("\"TestCategory\""));
        Document taskPatch = new Document();
        taskPatch.setData(Nullable.of(data));
        JsonPath jsonPath = pathBuilder.build("/tasks/" + task.getId());
        ResourcePatchController sut = new ResourcePatchController();
        sut.init(controllerContext);

        // WHEN
        Response response = sut.handle(jsonPath, emptyTaskQuery, taskPatch);

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
