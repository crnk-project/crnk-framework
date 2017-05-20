package io.crnk.core.engine.internal.dispatcher.controller.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jayway.jsonpath.ReadContext;
import io.crnk.core.engine.dispatcher.Response;
import io.crnk.core.engine.internal.dispatcher.controller.BaseControllerTest;
import io.crnk.core.engine.internal.dispatcher.controller.RelationshipsResourceGet;
import io.crnk.core.engine.internal.dispatcher.path.JsonPath;
import io.crnk.core.engine.internal.dispatcher.path.ResourcePath;
import io.crnk.core.engine.internal.utils.ClassUtils;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.mock.models.ProjectPolymorphic;
import io.crnk.core.mock.models.Task;
import io.crnk.core.mock.repository.ProjectPolymorphicToObjectRepository;
import io.crnk.core.mock.repository.TaskToProjectRepository;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.legacy.internal.QueryParamsAdapter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static net.javacrumbs.jsonunit.fluent.JsonFluentAssert.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class RelationshipsResourceGetTest extends BaseControllerTest {

	private static final String REQUEST_TYPE = "GET";
	private TaskToProjectRepository localTaskToProjectRepository;

	@Before
	public void prepareTest() throws Exception {
		localTaskToProjectRepository = new TaskToProjectRepository();
		localTaskToProjectRepository.removeRelations("project");
	}

	@Test
	public void onValidRequestShouldAcceptIt() {
		// GIVEN
		JsonPath jsonPath = pathBuilder.buildPath("tasks/1/relationships/project");
		ResourceRegistry resourceRegistry = mock(ResourceRegistry.class);
		RelationshipsResourceGet sut = new RelationshipsResourceGet(resourceRegistry, objectMapper, typeParser, documentMapper);

		// WHEN
		boolean result = sut.isAcceptable(jsonPath, REQUEST_TYPE);

		// THEN
		assertThat(result).isTrue();
	}

	@Test
	public void onFieldRequestShouldDenyIt() {
		// GIVEN
		JsonPath jsonPath = new ResourcePath("tasks/1/project");
		ResourceRegistry resourceRegistry = mock(ResourceRegistry.class);
		RelationshipsResourceGet sut = new RelationshipsResourceGet(resourceRegistry, objectMapper, typeParser, documentMapper);

		// WHEN
		boolean result = sut.isAcceptable(jsonPath, REQUEST_TYPE);

		// THEN
		assertThat(result).isFalse();
	}

	@Test
	public void onNonRelationRequestShouldDenyIt() {
		// GIVEN
		JsonPath jsonPath = new ResourcePath("tasks");
		ResourceRegistry resourceRegistry = mock(ResourceRegistry.class);
		RelationshipsResourceGet sut = new RelationshipsResourceGet(resourceRegistry, objectMapper, typeParser, documentMapper);

		// WHEN
		boolean result = sut.isAcceptable(jsonPath, REQUEST_TYPE);

		// THEN
		assertThat(result).isFalse();
	}

	@Test
	public void onGivenRequestLinkResourceGetShouldReturnNullData() throws Exception {
		// GIVEN

		JsonPath jsonPath = pathBuilder.buildPath("/tasks/1/relationships/project");
		RelationshipsResourceGet sut = new RelationshipsResourceGet(resourceRegistry, objectMapper, typeParser, documentMapper);

		// WHEN
		Response response = sut.handle(jsonPath, new QueryParamsAdapter(REQUEST_PARAMS), null, null);

		// THEN
		Assert.assertNotNull(response);
	}

	@Test
	public void onGivenRequestLinkResourceGetShouldReturnDataField() throws Exception {
		// GIVEN
		JsonPath jsonPath = pathBuilder.buildPath("/tasks/1/relationships/project");
		RelationshipsResourceGet sut = new RelationshipsResourceGet(resourceRegistry, objectMapper, typeParser, documentMapper);
		new TaskToProjectRepository().setRelation(new Task().setId(1L), 42L, "project");

		// WHEN
		Response response = sut.handle(jsonPath, new QueryParamsAdapter(REQUEST_PARAMS), null, null);

		// THEN
		Assert.assertNotNull(response);
		String resultJson = objectMapper.writeValueAsString(response.getDocument());
		assertThatJson(resultJson).node("data.id").isStringEqualTo("42");
		assertThatJson(resultJson).node("data.type").isEqualTo("projects");
	}

	@Test
	public void supportPolymorphicRelationshipTypes() throws JsonProcessingException {
		// GIVEN
		Long projectId = 1L;
		String type = ClassUtils.getAnnotation(ProjectPolymorphic.class, JsonApiResource.class).get().type();

		ProjectPolymorphic projectPolymorphic = new ProjectPolymorphic();
		projectPolymorphic.setId(projectId);
		ProjectPolymorphicToObjectRepository projectPolymorphicToObjectRepository = new ProjectPolymorphicToObjectRepository();
		projectPolymorphicToObjectRepository.setRelation(projectPolymorphic, 42L, "task");

		JsonPath jsonPath = pathBuilder.buildPath("/" + type + "/" + projectId + "/relationships/task");
		RelationshipsResourceGet resourceGet = new RelationshipsResourceGet(resourceRegistry, objectMapper, typeParser, documentMapper);

		// WHEN
		Response baseResponseContext = resourceGet.handle(jsonPath,
				new QueryParamsAdapter(REQUEST_PARAMS),
				null,
				null);
		// THEN
		Assert.assertNotNull(baseResponseContext);
		String resultJson = objectMapper.writeValueAsString(baseResponseContext.getDocument());
		assertThatJson(resultJson).node("data.id").isStringEqualTo("42");
		assertThatJson(resultJson).node("data.type").isEqualTo("tasks");

		// GIVEN
		projectPolymorphicToObjectRepository.setRelations(projectPolymorphic, Arrays.asList(44L, 45L), "tasks");
		jsonPath = pathBuilder.buildPath("/" + type + "/" + projectId + "/relationships/tasks");
		resourceGet = new RelationshipsResourceGet(resourceRegistry, objectMapper, typeParser, documentMapper);

		// WHEN
		baseResponseContext = resourceGet.handle(jsonPath,
				new QueryParamsAdapter(REQUEST_PARAMS),
				null,
				null);
		Assert.assertNotNull(baseResponseContext);

		resultJson = objectMapper.writeValueAsString(baseResponseContext.getDocument());
		ReadContext resultCtx = com.jayway.jsonpath.JsonPath.parse(resultJson);
		assertIncludeDoNotCareAboutOrder(new ArrayList<>(Arrays.asList("44", "45")), Arrays.asList(0, 1), resultCtx);

	}

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
