package io.crnk.core.engine.internal.resource;

import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.dispatcher.controller.BaseControllerTest;
import io.crnk.core.engine.internal.dispatcher.path.JsonPath;
import io.crnk.core.engine.internal.dispatcher.path.PathBuilder;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.module.SimpleModule;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.InMemoryResourceRepository;
import io.crnk.core.repository.RelationshipRepositoryV2;
import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiRelation;
import io.crnk.core.resource.annotations.JsonApiRelationId;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.core.resource.annotations.LookupIncludeBehavior;
import io.crnk.core.resource.annotations.RelationshipRepositoryBehavior;
import io.crnk.core.resource.list.ResourceList;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.Serializable;
import java.util.List;

public class NestedResourceTest extends BaseControllerTest {

	private TestRepository repository = new TestRepository();

	private ManyNestedRepository manyNestedRepository = new ManyNestedRepository();

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
		module.addRepository(manyNestedRepository);
		boot.addModule(module);
	}

	@Test
	public void checkNestedResourceInformation() {
		RegistryEntry testEntry = resourceRegistry.getEntry(TestResource.class);
		RegistryEntry nestedEntry = resourceRegistry.getEntry(ManyNestedResource.class);

		Assert.assertNotNull(testEntry);
		Assert.assertNotNull(nestedEntry);

		ResourceInformation nestedInformation = nestedEntry.getResourceInformation();
		ResourceField parentField = nestedInformation.findFieldByName("parent");
		Assert.assertTrue(parentField.hasIdField());
		Assert.assertEquals("children", parentField.getOppositeName());

		ManyNestedId nestedId = new ManyNestedId();
		nestedId.setId("a");
		nestedId.setParentId("b");
		ManyNestedResource nested = new ManyNestedResource();
		nested.setId(nestedId);
		Assert.assertEquals("b", parentField.getIdAccessor().getValue(nested));
	}

	@Test
	public void checkUrlComputation() {
		ManyNestedId nestedId = new ManyNestedId();
		nestedId.setId("a");
		nestedId.setParentId("b");
		ManyNestedResource nested = new ManyNestedResource();
		nested.setId(nestedId);


		Assert.assertEquals("http://localhost/test/b/children/1", resourceRegistry.getResourceUrl(nested));
		Assert.assertEquals("http://localhost/test/b/children/1", resourceRegistry.getResourceUrl(ManyNestedResource.class, "a-b"));
	}

	@Test
	public void checkAllPath(){
		/*PathBuilder pathBuilder = new PathBuilder(resourceRegistry);
		JsonPath path = pathBuilder.build("test/b/children");
		Assert.assertEquals("", path.getResourcePath());
		Assert.assertEquals("", path.getIds().getIds().get(0));
*/
	}

	/*
	@Test
	public void checkCrudWithController() throws Exception {
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
		Response response = postController.handleAsync(path, queryAdapter, null, document).get();
		Assert.assertEquals(HttpStatus.CREATED_201, response.getHttpStatus().intValue());

		Resource createdResource = response.getDocument().getSingleData().get();
		Assert.assertEquals("http://127.0.0.1/testA/b", createdResource.getLinks().get("self").asText());

		// PATCH resource
		document.setData(Nullable.of(createdResource));
		path = pathBuilder.build("/testA/b");
		createdResource.setAttribute("value", toJson("valueB"));
		Controller patchController = boot.getControllerRegistry().getController(path, HttpMethod.PATCH.toString());
		response = patchController.handleAsync(path, queryAdapter, null, document).get();
		Assert.assertEquals(HttpStatus.OK_200, response.getHttpStatus().intValue());

		// GET resource
		createdResource.setAttribute("value", toJson("valueB"));
		Controller getController = boot.getControllerRegistry().getController(path, HttpMethod.GET.toString());
		response = getController.handleAsync(path, queryAdapter, null, null).get();
		Assert.assertEquals(HttpStatus.OK_200, response.getHttpStatus().intValue());
		Resource getResource = response.getDocument().getSingleData().get();
		Assert.assertEquals("http://127.0.0.1/testA/b", getResource.getLinks().get("self").asText());

		// GET with inclusion with id
		QuerySpec includedQuerySpec = new QuerySpec(TestResourceB.class);
		includedQuerySpec.includeRelation(Arrays.asList("related"));
		QuerySpecAdapter includedQueryAdapter = container.toQueryAdapter(includedQuerySpec);
		response = getController.handleAsync(path, includedQueryAdapter, null, null).get();
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
		response = getController.handleAsync(path, includedQueryAdapter, null, null).get();
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
		response = deleteController.handleAsync(path, queryAdapter, null, null).get();
		Assert.assertEquals(HttpStatus.NO_CONTENT_204, response.getHttpStatus().intValue());
		try {
			repository.findOne("b", new QuerySpec(TestResourceA.class));
			Assert.fail();
		} catch (ResourceNotFoundException e) {
			// ok
		}
	}
*/

	@JsonApiResource(type = "test")
	public static class TestResource {

		@JsonApiId
		private String id;

		@JsonApiRelation(lookUp = LookupIncludeBehavior.AUTOMATICALLY_WHEN_NULL, opposite = "test", repositoryBehavior = RelationshipRepositoryBehavior.FORWARD_OPPOSITE)
		private List<ManyNestedResource> manyNested;

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public List<ManyNestedResource> getManyNested() {
			return manyNested;
		}

		public void setManyNested(List<ManyNestedResource> manyNested) {
			this.manyNested = manyNested;
		}
	}

	public static class ManyNestedId implements Serializable {

		private String id;

		@JsonApiRelationId
		private String parentId;

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getParentId() {
			return parentId;
		}

		public void setParentId(String parentId) {
			this.parentId = parentId;
		}

		public static ManyNestedId parse(String idString) {
			String[] elements = idString.split("\\-");
			ManyNestedId id = new ManyNestedId();
			id.parentId = elements[0];
			id.id = elements[1];
			return id;
		}

		public String toString() {
			return parentId + "-" + id;
		}
	}

	@JsonApiResource(type = "nested")
	public static class ManyNestedResource {

		@JsonApiId
		private ManyNestedId id;

		private String value;

		@JsonApiRelationId
		private String relatedId;

		@JsonApiRelation(lookUp = LookupIncludeBehavior.AUTOMATICALLY_WHEN_NULL, repositoryBehavior = RelationshipRepositoryBehavior.FORWARD_OWNER)
		private RelatedResource related;

		@JsonApiRelation(lookUp = LookupIncludeBehavior.AUTOMATICALLY_WHEN_NULL, repositoryBehavior = RelationshipRepositoryBehavior.FORWARD_OWNER)
		private TestResource parent;

		public ManyNestedId getId() {
			return id;
		}

		public void setId(ManyNestedId id) {
			this.id = id;
		}

		public TestResource getParent() {
			return parent;
		}

		public void setParent(TestResource parent) {
			this.parent = parent;
		}

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

	public static class TestRepository extends InMemoryResourceRepository<TestResource, String> {

		protected TestRepository() {
			super(TestResource.class);
		}
	}

	public static class ManyNestedRepository extends InMemoryResourceRepository<ManyNestedResource, ManyNestedId> {

		protected ManyNestedRepository() {
			super(ManyNestedResource.class);
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
			return ManyNestedResource.class;
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
