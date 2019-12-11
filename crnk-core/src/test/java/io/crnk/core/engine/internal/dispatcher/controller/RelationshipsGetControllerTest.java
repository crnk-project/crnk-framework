package io.crnk.core.engine.internal.dispatcher.controller;

import com.jayway.jsonpath.ReadContext;
import io.crnk.core.engine.dispatcher.Response;
import io.crnk.core.engine.internal.dispatcher.path.JsonPath;
import io.crnk.core.mock.models.Project;
import io.crnk.core.mock.models.Task;
import io.crnk.core.mock.repository.TaskToProjectRepository;
import io.crnk.core.repository.ResourceRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static net.javacrumbs.jsonunit.fluent.JsonFluentAssert.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

public class RelationshipsGetControllerTest extends ControllerTestBase {

    private static final String REQUEST_TYPE = "GET";

    private TaskToProjectRepository localTaskToProjectRepository;

    @Before
    public void prepareTest() {
        localTaskToProjectRepository = (TaskToProjectRepository) container.getRepository(Task.class, "project");
      //  localTaskToProjectRepository.removeRelations("project");
    }

    @Test
    public void onValidRequestShouldAcceptIt() {
        // GIVEN
        JsonPath jsonPath = pathBuilder.build("tasks/1/relationships/project", queryContext);
        RelationshipsResourceGetController sut = new RelationshipsResourceGetController();
        sut.init(controllerContext);

        // WHEN
        boolean result = sut.isAcceptable(jsonPath, REQUEST_TYPE);

        // THEN
        assertThat(result).isTrue();
    }

    @Test
    public void onFieldRequestShouldDenyIt() {
        // GIVEN
        JsonPath jsonPath = pathBuilder.build("tasks/1/project", queryContext);
        RelationshipsResourceGetController sut = new RelationshipsResourceGetController();
        sut.init(controllerContext);

        // WHEN
        boolean result = sut.isAcceptable(jsonPath, REQUEST_TYPE);

        // THEN
        assertThat(result).isFalse();
    }

    @Test
    public void onNonRelationRequestShouldDenyIt() {
        // GIVEN
        JsonPath jsonPath = pathBuilder.build("tasks", queryContext);
        RelationshipsResourceGetController sut = new RelationshipsResourceGetController();
        sut.init(controllerContext);

        // WHEN
        boolean result = sut.isAcceptable(jsonPath, REQUEST_TYPE);

        // THEN
        assertThat(result).isFalse();
    }

    @Test
    public void onGivenRequestLinkResourceGetShouldReturnNullData() {
        // GIVEN

        JsonPath jsonPath = pathBuilder.build("/tasks/1/relationships/project", queryContext);
        RelationshipsResourceGetController sut = new RelationshipsResourceGetController();
        sut.init(controllerContext);

        // WHEN
        Response response = sut.handle(jsonPath, emptyProjectQuery, null);

        // THEN
        Assert.assertNotNull(response);
    }

    @Test
    public void onGivenRequestLinkResourceGetShouldReturnDataField() throws Exception {
        ResourceRepository<Project, Object> projectRepository = container.getRepository(Project.class);
        Project project = new Project();
        project.setId(42L);
        projectRepository.save(project);

        // GIVEN
        JsonPath jsonPath = pathBuilder.build("/tasks/1/relationships/project", queryContext);
        RelationshipsResourceGetController sut = new RelationshipsResourceGetController();
        sut.init(controllerContext);
        TaskToProjectRepository relationship = (TaskToProjectRepository) container.getRepository(Task.class, "project");
        relationship.setRelation(new Task().setId(1L), 42L, "project");

        // WHEN
        Response response = sut.handle(jsonPath, emptyProjectQuery, null);

        // THEN
        Assert.assertNotNull(response);
        String resultJson = objectMapper.writeValueAsString(response.getDocument());
        assertThatJson(resultJson).node("data.id").isStringEqualTo("42");
        assertThatJson(resultJson).node("data.type").isEqualTo("projects");
        assertThatJson(resultJson).node("data.attributes").isAbsent();
    }

	/*
	@Test
	public void supportPolymorphicRelationshipTypes() throws JsonProcessingException {
		// GIVEN
		Long projectId = 1L;
		String type = ClassUtils.getAnnotation(ProjectPolymorphic.class, JsonApiResource.class).get().type();

		ProjectPolymorphic projectPolymorphic = new ProjectPolymorphic();
		projectPolymorphic.setId(projectId);
		ProjectPolymorphicToObjectRepository projectPolymorphicToObjectRepository = new ProjectPolymorphicToObjectRepository();
		projectPolymorphicToObjectRepository.setRelation(projectPolymorphic, 42L, "task");

		JsonPath jsonPath = pathBuilder.build("/" + type + "/" + projectId + "/relationships/task");
		RelationshipsResourceGetController resourceGet = new RelationshipsResourceGetController();
		resourceGet.init(controllerContext);

		// WHEN
		Response baseResponseContext = resourceGet.handle(jsonPath,
				emptyTaskQuery,
				null);
		// THEN
		Assert.assertNotNull(baseResponseContext);
		String resultJson = objectMapper.writeValueAsString(baseResponseContext.getDocument());
		assertThatJson(resultJson).node("data.id").isStringEqualTo("42");
		assertThatJson(resultJson).node("data.type").isEqualTo("tasks");

		// GIVEN
		projectPolymorphicToObjectRepository.setRelations(projectPolymorphic, Arrays.asList(44L, 45L), "tasks");
		jsonPath = pathBuilder.build("/" + type + "/" + projectId + "/relationships/tasks");
		resourceGet = new RelationshipsResourceGetController();
		resourceGet.init(controllerContext);

		// WHEN
		baseResponseContext = resourceGet.handle(jsonPath,
				emptyTaskQuery,
				null);
		Assert.assertNotNull(baseResponseContext);

		resultJson = objectMapper.writeValueAsString(baseResponseContext.getDocument());
		ReadContext resultCtx = com.jayway.jsonpath.JsonPath.parse(resultJson);
		assertIncludeDoNotCareAboutOrder(new ArrayList<>(Arrays.asList("44", "45")), Arrays.asList(0, 1), resultCtx);

	}
	*/

    private void assertIncludeDoNotCareAboutOrder(List<String> ids, List<Integer> indexes, ReadContext resultCtx) {

        for (Integer index : indexes) {
            assertEquals("tasks", resultCtx.read("data[" + index + "].type"));
        }

        for (Iterator<String> iterator = ids.iterator(); iterator.hasNext(); ) {
            String id = iterator.next();
            for (Integer index : indexes) {
                String idStr = resultCtx.read("data[" + index + "].id").toString();
                if (id.equals(idStr)) {
                    iterator.remove();
                }
            }
        }

        assertTrue("Could not find ids" + ids, ids.size() == 0);


    }
}
