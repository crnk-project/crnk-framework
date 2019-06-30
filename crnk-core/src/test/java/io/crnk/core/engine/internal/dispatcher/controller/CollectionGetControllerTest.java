package io.crnk.core.engine.internal.dispatcher.controller;

import io.crnk.core.engine.dispatcher.Response;
import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.document.Relationship;
import io.crnk.core.engine.document.Resource;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.internal.dispatcher.path.JsonPath;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.mock.models.Project;
import io.crnk.core.mock.models.Task;
import io.crnk.core.queryspec.PathSpec;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ManyRelationshipRepository;
import io.crnk.core.resource.annotations.SerializeType;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.core.utils.Nullable;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class CollectionGetControllerTest extends ControllerTestBase {

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
    public void onGivenRequestResourceWithIdShouldSetIt() {
        // GIVEN
        Document Document = new Document();
        Resource data = new Resource();
        Document.setData(Nullable.of(data));
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
    public void onGivenRequestResourceShouldLoadAutoIncludeFields() {
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
        newTaskToProjectBody.setData(Nullable.of(Collections.singletonList(data)));
        data.setType("projects");
        data.setId(projectId.toString());

        JsonPath savedTaskPath = pathBuilder.build("/tasks/" + taskId + "/relationships/includedProjects");
        RelationshipsPostController sut = new RelationshipsPostController();
        sut.init(controllerContext);

        // WHEN -- adding a relation between task and project
        Response projectRelationshipResponse = sut.handle(savedTaskPath, emptyProjectQuery, newTaskToProjectBody);
        assertThat(projectRelationshipResponse).isNotNull();

        // THEN
        ManyRelationshipRepository taskToProjectRepository = (ManyRelationshipRepository) container.getRepository(Task.class, "includedProjects");
        Map<Long, List<Project>> map = taskToProjectRepository.findManyRelations(Arrays.asList(taskId), "includedProjects", new QuerySpec(Project.class));
        Assert.assertEquals(1, map.size());
        List<Project> projects = map.get(taskId);
        assertThat(projects.get(0).getId()).isEqualTo(projectId);

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
    public void onGivenRequestResourceShouldNotLoadAutoIncludeFields() {
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
        newTaskToProjectBody.setData(Nullable.of(Collections.singletonList(data)));
        data.setType("projects");
        data.setId(projectId.toString());

        JsonPath savedTaskPath = pathBuilder.build("/tasks/" + taskId + "/relationships/projects");
        RelationshipsPostController sut =
                new RelationshipsPostController();
        sut.init(controllerContext);

        // WHEN -- adding a relation between task and project
        Response projectRelationshipResponse = sut.handle(savedTaskPath, emptyProjectQuery, newTaskToProjectBody);
        assertThat(projectRelationshipResponse).isNotNull();

        // THEN
        ManyRelationshipRepository taskToProjectRepository = (ManyRelationshipRepository) container.getRepository(Task.class, "projects");
        Map<Long, ResourceList<Project>> map = taskToProjectRepository.findManyRelations(Arrays.asList(taskId), "projects", new QuerySpec(Project.class));
        Assert.assertEquals(1, map.size());
        ResourceList<Project> projects = map.get(taskId);
        Assert.assertEquals(1, projects.size());
        assertThat(projects.get(0).getId()).isNotNull();

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
