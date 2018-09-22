package io.crnk.core.engine.internal.resource;

import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.dispatcher.Response;
import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.document.Relationship;
import io.crnk.core.engine.document.Resource;
import io.crnk.core.engine.document.ResourceIdentifier;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.http.HttpStatus;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.dispatcher.controller.BaseControllerTest;
import io.crnk.core.engine.internal.dispatcher.controller.Controller;
import io.crnk.core.engine.internal.dispatcher.path.JsonPath;
import io.crnk.core.engine.internal.repository.RelationshipRepositoryAdapter;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.core.module.SimpleModule;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.queryspec.internal.QuerySpecAdapter;
import io.crnk.core.repository.InMemoryResourceRepository;
import io.crnk.core.repository.RelationshipRepositoryV2;
import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiRelation;
import io.crnk.core.resource.annotations.JsonApiRelationId;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.core.resource.annotations.LookupIncludeBehavior;
import io.crnk.core.resource.annotations.RelationshipRepositoryBehavior;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.core.utils.Nullable;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public class InheritanceWithoutSubtypeRepositoryTest extends BaseControllerTest {

	private TestRepositoryA repository = new TestRepositoryA();

	private RelatedRepository relatedRepository = new RelatedRepository();

	private RelationshipRepository relationshipRepository = new RelationshipRepository();

	@Before
	public void before() {
		repository.clear();
		relatedRepository.clear();

		for (int i = 0; i < 5; i++) {
			RelatedResource relatedResource = new RelatedResource();
			relatedResource.setId("related" + i);
			relatedRepository.create(relatedResource);
		}
	}

	@Override
	protected void setup(CrnkBoot boot) {
		SimpleModule module = new SimpleModule("inheritance");
		module.addRepository(repository);
		module.addRepository(relatedRepository);
		module.addRepository(relationshipRepository);
		boot.addModule(module);
	}

	@Test
	public void checkSubTypeRegistered() {
		RegistryEntry entryA = resourceRegistry.getEntry(TestResourceA.class);
		RegistryEntry entryB = resourceRegistry.getEntry(TestResourceB.class);

		Assert.assertNotNull(entryA);
		Assert.assertNotNull(entryB);
		Assert.assertNotEquals(entryA, entryB);

		Assert.assertNull(entryB.getRepositoryInformation());
		Assert.assertFalse(entryB.hasResourceRepository());
		Assert.assertTrue(entryA.hasResourceRepository());

		ResourceInformation resourceInformationB = entryB.getResourceInformation();
		Assert.assertEquals("testB", resourceInformationB.getResourceType());
		Assert.assertEquals("testA", resourceInformationB.getResourcePath());

		RelationshipRepositoryAdapter relatedAdapter = entryB.getRelationshipRepository("related");
		Assert.assertNotNull(relatedAdapter);
	}


	@Test
	public void checkCrudWithController() {
		// CREATE resource
		Relationship relationship = new Relationship();
		relationship.setData(Nullable.of(new ResourceIdentifier("related0", "related")));
		Resource resource = new Resource();
		resource.setType("testB");
		resource.setId("b");
		resource.getRelationships().put("related", relationship);
		Document document = new Document();
		document.setData(Nullable.of(resource));
		JsonPath path = pathBuilder.build("/testA");

		QuerySpecAdapter queryAdapter = container.toQueryAdapter(new QuerySpec(TestResourceB.class));
		Controller postController = boot.getControllerRegistry().getController(path, HttpMethod.POST.toString());
		Response response = postController.handleAsync(path, queryAdapter, document).get();
		Assert.assertEquals(HttpStatus.CREATED_201, response.getHttpStatus().intValue());

		Resource createdResource = response.getDocument().getSingleData().get();
		Assert.assertEquals("http://127.0.0.1/testA/b", createdResource.getLinks().get("self").asText());

		// PATCH resource
		document.setData(Nullable.of(createdResource));
		path = pathBuilder.build("/testA/b");
		createdResource.setAttribute("value", toJson("valueB"));
		Controller patchController = boot.getControllerRegistry().getController(path, HttpMethod.PATCH.toString());
		response = patchController.handleAsync(path, queryAdapter, document).get();
		Assert.assertEquals(HttpStatus.OK_200, response.getHttpStatus().intValue());

		// GET resource
		createdResource.setAttribute("value", toJson("valueB"));
		Controller getController = boot.getControllerRegistry().getController(path, HttpMethod.GET.toString());
		response = getController.handleAsync(path, queryAdapter, null).get();
		Assert.assertEquals(HttpStatus.OK_200, response.getHttpStatus().intValue());
		Resource getResource = response.getDocument().getSingleData().get();
		Assert.assertEquals("http://127.0.0.1/testA/b", getResource.getLinks().get("self").asText());

		// GET with inclusion with id
		QuerySpec includedQuerySpec = new QuerySpec(TestResourceB.class);
		includedQuerySpec.includeRelation(Arrays.asList("related"));
		QuerySpecAdapter includedQueryAdapter = container.toQueryAdapter(includedQuerySpec);
		response = getController.handleAsync(path, includedQueryAdapter, null).get();
		Assert.assertEquals(HttpStatus.OK_200, response.getHttpStatus().intValue());
		getResource = response.getDocument().getSingleData().get();
		Assert.assertEquals("http://127.0.0.1/testA/b", getResource.getLinks().get("self").asText());
		List<Resource> included = response.getDocument().getIncluded();
		Assert.assertEquals(1, included.size());
		Resource includedResource = included.get(0);
		Assert.assertEquals("related0", includedResource.getId());

		// GET with inclusion with repository
		includedQuerySpec = new QuerySpec(TestResourceB.class);
		includedQuerySpec.includeRelation(Arrays.asList("relatedWithRepository"));
		includedQueryAdapter = container.toQueryAdapter(includedQuerySpec);
		response = getController.handleAsync(path, includedQueryAdapter, null).get();
		Assert.assertEquals(HttpStatus.OK_200, response.getHttpStatus().intValue());
		getResource = response.getDocument().getSingleData().get();
		Assert.assertEquals("http://127.0.0.1/testA/b", getResource.getLinks().get("self").asText());
		included = response.getDocument().getIncluded();
		Assert.assertEquals(1, included.size());
		includedResource = included.get(0);
		Assert.assertEquals("related1", includedResource.getId());

		// DELETE resource
		Assert.assertNotNull(repository.findOne("b", new QuerySpec(TestResourceA.class)));
		createdResource.setAttribute("value", toJson("valueB"));
		Controller deleteController = boot.getControllerRegistry().getController(path, HttpMethod.DELETE.toString());
		response = deleteController.handleAsync(path, queryAdapter, null).get();
		Assert.assertEquals(HttpStatus.NO_CONTENT_204, response.getHttpStatus().intValue());
		try {
			repository.findOne("b", new QuerySpec(TestResourceA.class));
			Assert.fail();
		} catch (ResourceNotFoundException e) {
			// ok
		}
	}


	@JsonApiResource(type = "testA", subTypes = TestResourceB.class)
	public static class TestResourceA {

		@JsonApiId
		private String id;

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}
	}

	@JsonApiResource(type = "testB", resourcePath = "testA")
	public static class TestResourceB extends TestResourceA {

		private String value;

		@JsonApiRelationId
		private String relatedId;

		@JsonApiRelation(lookUp = LookupIncludeBehavior.AUTOMATICALLY_WHEN_NULL, repositoryBehavior = RelationshipRepositoryBehavior.FORWARD_OWNER)
		private RelatedResource related;

		@JsonApiRelation(lookUp = LookupIncludeBehavior.AUTOMATICALLY_WHEN_NULL)
		private RelatedResource relatedWithRepository;

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}

		public String getRelatedId() {
			return relatedId;
		}

		public void setRelatedId(String relatedId) {
			this.relatedId = relatedId;
		}

		public RelatedResource getRelated() {
			return related;
		}

		public void setRelated(RelatedResource related) {
			this.related = related;
		}

		public RelatedResource getRelatedWithRepository() {
			return relatedWithRepository;
		}

		public void setRelatedWithRepository(RelatedResource relatedWithRepository) {
			this.relatedWithRepository = relatedWithRepository;
		}
	}

	@JsonApiResource(type = "related")
	public static class RelatedResource {

		@JsonApiId
		private String id;

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}
	}

	public static class TestRepositoryA extends InMemoryResourceRepository<TestResourceA, String> {

		protected TestRepositoryA() {
			super(TestResourceA.class);
		}
	}

	public static class RelatedRepository extends InMemoryResourceRepository<RelatedResource, String> {

		protected RelatedRepository() {
			super(RelatedResource.class);
		}
	}

	public static class RelationshipRepository implements RelationshipRepositoryV2 {

		@Override
		public Class getSourceResourceClass() {
			return TestResourceB.class;
		}

		@Override
		public Class getTargetResourceClass() {
			return RelatedResource.class;
		}

		@Override
		public void setRelation(Object source, Serializable targetId, String fieldName) {

		}

		@Override
		public void setRelations(Object source, Iterable targetIds, String fieldName) {

		}

		@Override
		public void addRelations(Object source, Iterable targetIds, String fieldName) {

		}

		@Override
		public void removeRelations(Object source, Iterable targetIds, String fieldName) {

		}

		@Override
		public Object findOneTarget(Serializable sourceId, String fieldName, QuerySpec querySpec) {
			RelatedResource related = new RelatedResource();
			related.setId("related1");
			return related;
		}

		@Override
		public ResourceList findManyTargets(Serializable sourceId, String fieldName, QuerySpec querySpec) {
			return null;
		}
	}
}
