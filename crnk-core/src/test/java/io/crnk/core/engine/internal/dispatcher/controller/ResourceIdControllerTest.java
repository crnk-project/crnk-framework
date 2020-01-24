package io.crnk.core.engine.internal.dispatcher.controller;

import io.crnk.core.engine.dispatcher.Response;
import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.document.Relationship;
import io.crnk.core.engine.document.Resource;
import io.crnk.core.engine.document.ResourceIdentifier;
import io.crnk.core.engine.http.HttpStatus;
import io.crnk.core.engine.internal.dispatcher.path.JsonPath;
import io.crnk.core.mock.models.RelationIdTestResource;
import io.crnk.core.mock.models.Schedule;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.core.utils.Nullable;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * checks usage of @JsonApiRelationId
 */
public class ResourceIdControllerTest extends ControllerTestBase {

	private static final String REQUEST_TYPE = "POST";

	private Schedule schedule2;

	private Schedule schedule3;

	private ResourceIdentifier schedule3Id;

	private ResourceIdentifier schedule2Id;

	private ResourceRepository<RelationIdTestResource, Object> repository;

	@Before
	public void prepare() {
		super.prepare();

		ResourceRepository<Schedule, Object> scheduleRepository = container.getRepository(Schedule.class);

		schedule2 = new Schedule();
		schedule2.setId(2L);
		schedule2.setName("test");
		scheduleRepository.save(schedule2);
		schedule2Id = new ResourceIdentifier(schedule2.getId().toString(), "schedules");

		schedule3 = new Schedule();
		schedule3.setId(3L);
		schedule3.setName("test");
		scheduleRepository.save(schedule3);
		schedule3Id = new ResourceIdentifier(schedule3.getId().toString(), "schedules");

		repository = container.getRepository(RelationIdTestResource.class);
	}

	public Resource createResource() throws IOException {
		Resource data = new Resource();
		data.setType("relationIdTest");
		data.setId("1");
		data.setAttribute("name", objectMapper.readTree("\"test\""));
		return data;
	}

	public Document createDocument(Object object) {
		Document newDocument = new Document();
		newDocument.setData(Nullable.of(object));
		return newDocument;
	}

	@Test
	public void check() throws Exception {
		Resource resource = checkPost();
		resource = checkPatchResource(resource);
		checkPatchRelationship(resource, schedule3Id);
		checkPatchRelationship(resource, null);
	}

	private Resource checkPost() throws IOException {
		// GIVEN
		Resource resource = createResource();
		resource.getRelationships().put("testLookupAlways", new Relationship(schedule3Id));
		Document newDocument = createDocument(resource);

		JsonPath postPath = pathBuilder.build("/relationIdTest", queryContext);

		// WHEN POST
		ResourcePostController sut = new ResourcePostController();
		sut.init(controllerContext);

		Response taskResponse = sut.handle(postPath, emptyTaskQuery, newDocument);

		// THEN POST
		assertThat(taskResponse.getHttpStatus()).isEqualTo(HttpStatus.CREATED_201);
		Resource createdResource = taskResponse.getDocument().getSingleData().get();

		Assert.assertEquals(resource.getType(), createdResource.getType());
		Assert.assertEquals(resource.getId(), createdResource.getId());
		Assert.assertEquals(resource.getAttributes().get("name"), createdResource.getAttributes().get("name"));

		Relationship relationship = createdResource.getRelationships().get("testLookupAlways");
		Assert.assertTrue(relationship.getData().isPresent());
		Assert.assertEquals(schedule3Id, relationship.getData().get());

		// validate relationship not accessed, only id set => performance
		RelationIdTestResource entity = repository.findOne(1L, new QuerySpec(RelationIdTestResource.class));
		Assert.assertEquals(schedule3.getId(), entity.getTestLookupAlwaysId());
		Assert.assertNull(entity.getTestLookupAlways());

		return createdResource;

	}

	private Resource checkPatchResource(Resource resource) {
		JsonPath path = pathBuilder.build("/relationIdTest/" + resource.getId(), queryContext);

		resource.getRelationships().put("testLookupAlways", new Relationship(schedule2Id));
		Document newDocument = createDocument(resource);

		// WHEN PATCH
		ResourcePatchController sut = new ResourcePatchController();
		sut.init(controllerContext);

		Response taskResponse = sut.handle(path, emptyTaskQuery, newDocument);

		// THEN PATCH
		assertThat(taskResponse.getHttpStatus()).isEqualTo(HttpStatus.OK_200);
		Resource updatedResource = taskResponse.getDocument().getSingleData().get();

		Assert.assertEquals(resource.getType(), updatedResource.getType());
		Assert.assertEquals(resource.getId(), updatedResource.getId());
		Assert.assertEquals(resource.getAttributes().get("name"), updatedResource.getAttributes().get("name"));

		Relationship relationship = updatedResource.getRelationships().get("testLookupAlways");
		Assert.assertTrue(relationship.getData().isPresent());
		Assert.assertEquals(schedule2Id, relationship.getData().get());

		// validate relationship not accessed, only id set => performance
		RelationIdTestResource entity = repository.findOne(1L, new QuerySpec(RelationIdTestResource.class));
		Assert.assertEquals(schedule2.getId(), entity.getTestLookupAlwaysId());
		Assert.assertNull(entity.getTestLookupAlways());

		return updatedResource;
	}

	private void checkPatchRelationship(Resource resource, ResourceIdentifier scheduleId) {

		JsonPath path = pathBuilder.build("/relationIdTest/" + resource.getId() + "/relationships/testLookupAlways", queryContext);
		Document newDocument = createDocument(scheduleId);

		// WHEN PATCH
		RelationshipsPatchController sut = new RelationshipsPatchController();
		sut.init(controllerContext);

		Response taskResponse = sut.handle(path, emptyTaskQuery, newDocument);

		// THEN PATCH
		assertThat(taskResponse.getHttpStatus()).isEqualTo(HttpStatus.NO_CONTENT_204);

		// validate relationship not accessed, only id set => performance
		RelationIdTestResource entity = repository.findOne(1L, new QuerySpec(RelationIdTestResource.class));
		Assert.assertEquals(scheduleId != null ? Long.parseLong(scheduleId.getId()) : null, entity.getTestLookupAlwaysId());
		Assert.assertNull(entity.getTestLookupAlways());
	}
}
