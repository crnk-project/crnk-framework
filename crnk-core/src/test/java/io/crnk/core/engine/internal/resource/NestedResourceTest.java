package io.crnk.core.engine.internal.resource;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.dispatcher.Response;
import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.document.Relationship;
import io.crnk.core.engine.document.Resource;
import io.crnk.core.engine.document.ResourceIdentifier;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.http.HttpStatus;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.dispatcher.controller.ControllerTestBase;
import io.crnk.core.engine.internal.dispatcher.controller.Controller;
import io.crnk.core.engine.internal.dispatcher.path.FieldPath;
import io.crnk.core.engine.internal.dispatcher.path.JsonPath;
import io.crnk.core.engine.internal.dispatcher.path.PathBuilder;
import io.crnk.core.engine.internal.dispatcher.path.RelationshipsPath;
import io.crnk.core.engine.internal.dispatcher.path.ResourcePath;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.core.module.SimpleModule;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.queryspec.internal.QuerySpecAdapter;
import io.crnk.core.repository.InMemoryResourceRepository;
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

public class NestedResourceTest extends ControllerTestBase {

	private TestRepository repository = new TestRepository();

	private ManyNestedRepository manyNestedRepository = new ManyNestedRepository();

	private OneNestedRepository oneNestedRepository = new OneNestedRepository();

	private OneGrandchildRepository oneGrandchildRepository = new OneGrandchildRepository();

	private ManyGrandchildrenRepository manyGrandchildrenRepository = new ManyGrandchildrenRepository();

	private RelatedRepository relatedRepository = new RelatedRepository();

	private RelationshipRepository relationshipRepository = new RelationshipRepository();

	@Before
	public void before() {
		repository.clear();
		relatedRepository.clear();

		TestResource test = new TestResource();
		test.setId("b");
		repository.create(test);

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
		module.addRepository(oneNestedRepository);
		module.addRepository(manyNestedRepository);
		module.addRepository(oneGrandchildRepository);
		module.addRepository(manyGrandchildrenRepository);
		boot.addModule(module);
	}

	@Test
	public void checkNestedResourceInformation() {
		RegistryEntry testEntry = resourceRegistry.getEntry(TestResource.class);
		RegistryEntry manyNestedEntry = resourceRegistry.getEntry(ManyNestedResource.class);
		RegistryEntry oneNestedEntry = resourceRegistry.getEntry(OneNestedResource.class);

		Assert.assertNotNull(testEntry);
		Assert.assertNotNull(manyNestedEntry);
		Assert.assertNotNull(oneNestedEntry);

		ResourceInformation oneNestedInformation = oneNestedEntry.getResourceInformation();
		ResourceField oneParentField = oneNestedInformation.findFieldByName("parent");
		Assert.assertTrue(oneParentField.hasIdField());
		Assert.assertEquals("oneNested", oneParentField.getOppositeName());

		ResourceInformation manyNestedInformation = manyNestedEntry.getResourceInformation();
		ResourceField manyParentField = manyNestedInformation.findFieldByName("parent");
		Assert.assertTrue(manyParentField.hasIdField());
		Assert.assertEquals("manyNested", manyParentField.getOppositeName());

		ManyNestedId nestedId = new ManyNestedId();
		nestedId.setId("a");
		nestedId.setParentId("b");
		ManyNestedResource nested = new ManyNestedResource();
		nested.setId(nestedId);
		Assert.assertEquals("b", manyParentField.getIdAccessor().getValue(nested));
	}

	@Test
	public void checkOneUrlComputation() {
		OneNestedResource nested = new OneNestedResource();
		nested.setParentId("a");
		Assert.assertEquals("http://127.0.0.1/test/a/oneNested", resourceRegistry.getResourceUrl(nested));
	}

	@Test
	public void checkManyUrlComputation() {
		ManyNestedId nestedId = new ManyNestedId();
		nestedId.setId("b");
		nestedId.setParentId("a");
		ManyNestedResource nested = new ManyNestedResource();
		nested.setId(nestedId);

		Assert.assertEquals("http://127.0.0.1/test/a/manyNested/b", resourceRegistry.getResourceUrl(nested));
	}

	@Test
	public void checkOneFieldPath() {
		PathBuilder pathBuilder = new PathBuilder(resourceRegistry, typeParser);
		FieldPath path = (FieldPath) pathBuilder.build("test/b/oneNested/value", queryContext);
		Assert.assertEquals("oneNested", path.getRootEntry().getResourceInformation().getResourceType());
		Assert.assertEquals("b", path.getId());
		Assert.assertEquals("value", path.getField().getJsonName());
	}


	@Test
	public void checkManyFieldPath() {
		PathBuilder pathBuilder = new PathBuilder(resourceRegistry, typeParser);
		FieldPath path = (FieldPath) pathBuilder.build("test/b/manyNested", queryContext);
		Assert.assertEquals("test", path.getRootEntry().getResourceInformation().getResourceType());
		Assert.assertEquals("b", path.getId());
		Assert.assertEquals("manyNested", path.getField().getJsonName());
	}

	@Test
	public void checkOneNestedResourcePath() {
		PathBuilder pathBuilder = new PathBuilder(resourceRegistry, typeParser);
		ResourcePath path = (ResourcePath) pathBuilder.build("test/b/oneNested", queryContext);
		Assert.assertEquals("oneNested", path.getRootEntry().getResourceInformation().getResourceType());

		String id = (String) path.getId();
		Assert.assertEquals("b", id);
	}

	@Test
	public void checkManyNestedResourcePath() {
		PathBuilder pathBuilder = new PathBuilder(resourceRegistry, typeParser);
		ResourcePath path = (ResourcePath) pathBuilder.build("test/b/manyNested/a", queryContext);
		Assert.assertEquals("manyNested", path.getRootEntry().getResourceInformation().getResourceType());

		ManyNestedId id = (ManyNestedId) path.getId();
		Assert.assertEquals("a", id.getId());
		Assert.assertEquals("b", id.getParentId());
	}

	@Test
	public void checkOneRelationshipPath() {
		PathBuilder pathBuilder = new PathBuilder(resourceRegistry, typeParser);
		RelationshipsPath path = (RelationshipsPath) pathBuilder.build("test/b/oneNested/relationships/related", queryContext);
		Assert.assertEquals("oneNested", path.getRootEntry().getResourceInformation().getResourceType());

		String id = (String) path.getId();
		Assert.assertEquals("b", id);
		Assert.assertEquals("related", path.getRelationship().getJsonName());
		Assert.assertEquals("test/{id}/oneNested/relationships/related", path.toGroupPath());
	}

	@Test
	public void checkManyNestedRelationshipPath() {
		PathBuilder pathBuilder = new PathBuilder(resourceRegistry, typeParser);
		RelationshipsPath path = (RelationshipsPath) pathBuilder.build("test/b/manyNested/a/relationships/related", queryContext);
		Assert.assertEquals("manyNested", path.getRootEntry().getResourceInformation().getResourceType());

		ManyNestedId id = (ManyNestedId) path.getId();
		Assert.assertEquals("a", id.getId());
		Assert.assertEquals("b", id.getParentId());
		Assert.assertEquals("related", path.getRelationship().getJsonName());
		Assert.assertEquals("test/{id}/manyNested/{id}/relationships/related", path.toGroupPath());

	}

	@Test
	public void checkOneNestedFieldPath() {
		PathBuilder pathBuilder = new PathBuilder(resourceRegistry, typeParser);
		FieldPath path = (FieldPath) pathBuilder.build("test/b/oneNested/related", queryContext);
		Assert.assertEquals("oneNested", path.getRootEntry().getResourceInformation().getResourceType());

		String id = (String) path.getId();
		Assert.assertEquals("b", id);
		Assert.assertEquals("related", path.getField().getJsonName());
	}

	@Test
	public void checkManyNestedFieldPath() {
		PathBuilder pathBuilder = new PathBuilder(resourceRegistry, typeParser);
		FieldPath path = (FieldPath) pathBuilder.build("test/b/manyNested/a/related", queryContext);
		Assert.assertEquals("manyNested", path.getRootEntry().getResourceInformation().getResourceType());

		ManyNestedId id = (ManyNestedId) path.getId();
		Assert.assertEquals("a", id.getId());
		Assert.assertEquals("b", id.getParentId());
		Assert.assertEquals("related", path.getField().getJsonName());
	}

	@Test
	public void checkOneCrudWithController() {
		// CREATE resource
		Relationship relationship = new Relationship();
		relationship.setData(Nullable.of(new ResourceIdentifier("related0", "related")));
		Resource resource = new Resource();
		resource.setType("oneNested");
		resource.setId("b");
		resource.getRelationships().put("related", relationship);
		Document document = new Document();
		document.setData(Nullable.of(resource));
		JsonPath path = pathBuilder.build("test/b/oneNested", queryContext);

		QuerySpecAdapter queryAdapter = container.toQueryAdapter(new QuerySpec(ManyNestedResource.class));
		Controller postController = boot.getControllerRegistry().getController(path, HttpMethod.POST.toString());
		Response response = postController.handleAsync(path, queryAdapter, document).get();
		Assert.assertEquals(HttpStatus.CREATED_201, response.getHttpStatus().intValue());

		Resource createdResource = response.getDocument().getSingleData().get();
		Assert.assertEquals("http://127.0.0.1/test/b/oneNested", createdResource.getLinks().get("self").asText());

		// PATCH resource
		document.setData(Nullable.of(createdResource));
		path = pathBuilder.build("test/b/oneNested", queryContext);
		Assert.assertNotNull(path);
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
		Assert.assertEquals("http://127.0.0.1/test/b/oneNested", getResource.getLinks().get("self").asText());

		// GET with inclusion with id
		QuerySpec includedQuerySpec = new QuerySpec(ManyNestedResource.class);
		includedQuerySpec.includeRelation(Arrays.asList("related"));
		QuerySpecAdapter includedQueryAdapter = container.toQueryAdapter(includedQuerySpec);
		response = getController.handleAsync(path, includedQueryAdapter, null).get();
		Assert.assertEquals(HttpStatus.OK_200, response.getHttpStatus().intValue());
		getResource = response.getDocument().getSingleData().get();
		Assert.assertEquals("http://127.0.0.1/test/b/oneNested", getResource.getLinks().get("self").asText());
		List<Resource> included = response.getDocument().getIncluded();
		Assert.assertEquals(1, included.size());
		Resource includedResource = included.get(0);
		Assert.assertEquals("related0", includedResource.getId());

		// DELETE resource
		String id = "b";
		Assert.assertNotNull(oneNestedRepository.findOne(id, new QuerySpec(ManyNestedResource.class)));
		createdResource.setAttribute("value", toJson("valueB"));
		Controller deleteController = boot.getControllerRegistry().getController(path, HttpMethod.DELETE.toString());
		response = deleteController.handleAsync(path, queryAdapter, null).get();
		Assert.assertEquals(HttpStatus.NO_CONTENT_204, response.getHttpStatus().intValue());
		try {
			oneNestedRepository.findOne(id, new QuerySpec(ManyNestedResource.class));
			Assert.fail();
		}
		catch (ResourceNotFoundException e) {
			// ok
		}
	}

	@Test
	public void checkOneGrandchildCrudWithController() {
		// CREATE nested child resource
		Resource child = new Resource();
		child.setType("oneNested");
		child.setId("b");
		Document document = new Document();
		document.setData(Nullable.of(child));
		JsonPath childPath = pathBuilder.build("test/b/oneNested", queryContext);

		QuerySpecAdapter queryAdapter = container.toQueryAdapter(new QuerySpec(ManyNestedResource.class));
		Controller postController = boot.getControllerRegistry().getController(childPath, HttpMethod.POST.toString());
		Response response = postController.handleAsync(childPath, queryAdapter, document).get();
		Assert.assertEquals(HttpStatus.CREATED_201, response.getHttpStatus().intValue());

		Resource createdChild = response.getDocument().getSingleData().get();
		Assert.assertEquals("http://127.0.0.1/test/b/oneNested", createdChild.getLinks().get("self").asText());

		// CREATE nested grandchild resource under child
		Resource grandchild = new Resource();
		grandchild.setType("oneGrandchild");
		grandchild.setId("b");
		document = new Document();
		document.setData(Nullable.of(grandchild));
		response = postController.handleAsync(pathBuilder.build("test/b/oneNested/oneGrandchild", queryContext),
								   queryAdapter, document).get();
		Resource createdGrandchild = response.getDocument().getSingleData().get();
		Assert.assertEquals("http://127.0.0.1/test/b/oneNested/oneGrandchild", createdGrandchild.getLinks().get("self").asText());


		// PATCH resource
		document.setData(Nullable.of(createdGrandchild));
		JsonPath path = pathBuilder.build("test/b/oneNested/oneGrandchild", queryContext);
		Assert.assertNotNull(path);
		createdGrandchild.setAttribute("value", toJson("valueB"));
		Controller patchController = boot.getControllerRegistry().getController(path, HttpMethod.PATCH.toString());
		response = patchController.handleAsync(path, queryAdapter, document).get();
		Assert.assertEquals(HttpStatus.OK_200, response.getHttpStatus().intValue());

		// GET resource
		createdGrandchild.setAttribute("value", toJson("valueB"));
		Controller getController = boot.getControllerRegistry().getController(path, HttpMethod.GET.toString());
		response = getController.handleAsync(path, queryAdapter, null).get();
		Assert.assertEquals(HttpStatus.OK_200, response.getHttpStatus().intValue());
		Resource getResource = response.getDocument().getSingleData().get();
		Assert.assertEquals("http://127.0.0.1/test/b/oneNested/oneGrandchild", getResource.getLinks().get("self").asText());

		// GET with inclusion with id
		QuerySpec includedQuerySpec = new QuerySpec(ManyNestedResource.class);
		includedQuerySpec.includeRelation(Arrays.asList("parent"));
		QuerySpecAdapter includedQueryAdapter = container.toQueryAdapter(includedQuerySpec);
		response = getController.handleAsync(path, includedQueryAdapter, null).get();
		Assert.assertEquals(HttpStatus.OK_200, response.getHttpStatus().intValue());
		getResource = response.getDocument().getSingleData().get();
		Assert.assertEquals("http://127.0.0.1/test/b/oneNested/oneGrandchild", getResource.getLinks().get("self").asText());
		List<Resource> included = response.getDocument().getIncluded();
		Assert.assertEquals(1, included.size());
		Resource includedResource = included.get(0);
		Assert.assertEquals("b", includedResource.getId());
		Assert.assertEquals("oneNested", includedResource.getType());

		// DELETE resource
		String id = "b";
		Assert.assertNotNull(oneGrandchildRepository.findOne(id, new QuerySpec(ManyNestedResource.class)));
		createdGrandchild.setAttribute("value", toJson("valueB"));
		Controller deleteController = boot.getControllerRegistry().getController(path, HttpMethod.DELETE.toString());
		response = deleteController.handleAsync(path, queryAdapter, null).get();
		Assert.assertEquals(HttpStatus.NO_CONTENT_204, response.getHttpStatus().intValue());
		try {
			oneGrandchildRepository.findOne(id, new QuerySpec(ManyNestedResource.class));
			Assert.fail("resource found when should be deleted.");
		}
		catch (ResourceNotFoundException e) {
			// ok
		}

	}

	@Test
	public void checkManyCrudWithController() {
		// CREATE resource
		Relationship relationship = new Relationship();
		relationship.setData(Nullable.of(new ResourceIdentifier("related0", "related")));
		Resource resource = new Resource();
		resource.setType("manyNested");
		resource.setId("b-a");
		resource.getRelationships().put("related", relationship);
		Document document = new Document();
		document.setData(Nullable.of(resource));
		JsonPath path = pathBuilder.build("test/b/manyNested", queryContext);

		QuerySpecAdapter queryAdapter = container.toQueryAdapter(new QuerySpec(ManyNestedResource.class));
		Controller postController = boot.getControllerRegistry().getController(path, HttpMethod.POST.toString());
		Response response = postController.handleAsync(path, queryAdapter, document).get();
		Assert.assertEquals(HttpStatus.CREATED_201, response.getHttpStatus().intValue());

		Resource createdResource = response.getDocument().getSingleData().get();
		Assert.assertEquals("http://127.0.0.1/test/b/manyNested/a", createdResource.getLinks().get("self").asText());

		// PATCH resource
		document.setData(Nullable.of(createdResource));
		path = pathBuilder.build("test/b/manyNested/a", queryContext);
		Assert.assertNotNull(path);
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
		Assert.assertEquals("http://127.0.0.1/test/b/manyNested/a", getResource.getLinks().get("self").asText());

		// GET with inclusion with id
		QuerySpec includedQuerySpec = new QuerySpec(ManyNestedResource.class);
		includedQuerySpec.includeRelation(Arrays.asList("related"));
		QuerySpecAdapter includedQueryAdapter = container.toQueryAdapter(includedQuerySpec);
		response = getController.handleAsync(path, includedQueryAdapter, null).get();
		Assert.assertEquals(HttpStatus.OK_200, response.getHttpStatus().intValue());
		getResource = response.getDocument().getSingleData().get();
		Assert.assertEquals("http://127.0.0.1/test/b/manyNested/a", getResource.getLinks().get("self").asText());
		List<Resource> included = response.getDocument().getIncluded();
		Assert.assertEquals(1, included.size());
		Resource includedResource = included.get(0);
		Assert.assertEquals("related0", includedResource.getId());

		// DELETE resource
		ManyNestedId id = new ManyNestedId("b", "a");
		Assert.assertNotNull(manyNestedRepository.findOne(id, new QuerySpec(ManyNestedResource.class)));
		createdResource.setAttribute("value", toJson("valueB"));
		Controller deleteController = boot.getControllerRegistry().getController(path, HttpMethod.DELETE.toString());
		response = deleteController.handleAsync(path, queryAdapter, null).get();
		Assert.assertEquals(HttpStatus.NO_CONTENT_204, response.getHttpStatus().intValue());
		try {
			manyNestedRepository.findOne(id, new QuerySpec(ManyNestedResource.class));
			Assert.fail();
		}
		catch (ResourceNotFoundException e) {
			// ok
		}
	}

	@Test
	public void checkManyGrandchildrenCrudWithController() {
		// CREATE child resource
		Resource child = new Resource();
		child.setType("manyNested");
		child.setId("b-a");
		Document document = new Document();
		document.setData(Nullable.of(child));
		JsonPath childPath = pathBuilder.build("test/b/manyNested", queryContext);

		QuerySpecAdapter queryAdapter = container.toQueryAdapter(new QuerySpec(ManyNestedResource.class));
		Controller postController = boot.getControllerRegistry().getController(childPath, HttpMethod.POST.toString());
		Response response = postController.handleAsync(childPath, queryAdapter, document).get();
		Assert.assertEquals(HttpStatus.CREATED_201, response.getHttpStatus().intValue());

		Resource createdChild = response.getDocument().getSingleData().get();
		Assert.assertEquals("http://127.0.0.1/test/b/manyNested/a", createdChild.getLinks().get("self").asText());

		// CREATE grandchild
		Resource grandchild = new Resource();
		grandchild.setType("manyGrandchildren");
		grandchild.setId("b-a-c");
		document = new Document();
		document.setData(Nullable.of(grandchild));
		JsonPath grandchildPath = pathBuilder.build("test/b/manyNested/a/manyGrandchildren", queryContext);

		response = postController.handleAsync(grandchildPath, queryAdapter, document).get();
		Assert.assertEquals(HttpStatus.CREATED_201, response.getHttpStatus().intValue());

		Resource createdGrandchild = response.getDocument().getSingleData().get();
		Assert.assertEquals("http://127.0.0.1/test/b/manyNested/a/manyGrandchildren/c", createdGrandchild.getLinks().get("self").asText());

		// PATCH resource
		document.setData(Nullable.of(createdGrandchild));
		grandchildPath = pathBuilder.build("test/b/manyNested/a/manyGrandchildren/c", queryContext);
		Assert.assertNotNull(grandchildPath);
		createdGrandchild.setAttribute("value", toJson("valueC"));
		Controller patchController = boot.getControllerRegistry().getController(grandchildPath, HttpMethod.PATCH.toString());
		response = patchController.handleAsync(grandchildPath, queryAdapter, document).get();
		Assert.assertEquals(HttpStatus.OK_200, response.getHttpStatus().intValue());

		// GET resource
		createdGrandchild.setAttribute("value", toJson("valueB"));
		Controller getController = boot.getControllerRegistry().getController(grandchildPath, HttpMethod.GET.toString());
		response = getController.handleAsync(grandchildPath, queryAdapter, null).get();
		Assert.assertEquals(HttpStatus.OK_200, response.getHttpStatus().intValue());
		Resource getResource = response.getDocument().getSingleData().get();
		Assert.assertEquals("http://127.0.0.1/test/b/manyNested/a/manyGrandchildren/c", getResource.getLinks().get("self").asText());

		// GET with inclusion with id
		QuerySpec includedQuerySpec = new QuerySpec(ManyNestedResource.class);
		includedQuerySpec.includeRelation(Arrays.asList("parent"));
		QuerySpecAdapter includedQueryAdapter = container.toQueryAdapter(includedQuerySpec);
		response = getController.handleAsync(grandchildPath, includedQueryAdapter, null).get();
		Assert.assertEquals(HttpStatus.OK_200, response.getHttpStatus().intValue());
		getResource = response.getDocument().getSingleData().get();
		Assert.assertEquals("http://127.0.0.1/test/b/manyNested/a/manyGrandchildren/c", getResource.getLinks().get("self").asText());
		List<Resource> included = response.getDocument().getIncluded();
		//Assert.assertEquals(1, included.size());
		Resource includedResource = included.get(0);
		Assert.assertEquals("b-a", includedResource.getId());

		// DELETE resource
		ManyGrandchildrenId id = new ManyGrandchildrenId(new ManyNestedId("b", "a"), "c");
		Assert.assertNotNull(manyGrandchildrenRepository.findOne(id, new QuerySpec(ManyGrandchildrenResource.class)));
		Controller deleteController = boot.getControllerRegistry().getController(grandchildPath, HttpMethod.DELETE.toString());
		response = deleteController.handleAsync(grandchildPath, queryAdapter, null).get();
		Assert.assertEquals(HttpStatus.NO_CONTENT_204, response.getHttpStatus().intValue());
		try {
			manyGrandchildrenRepository.findOne(id, new QuerySpec(ManyNestedResource.class));
			Assert.fail();
		}
		catch (ResourceNotFoundException e) {
			// ok
		}
	}

	@JsonApiResource(type = "test")
	public static class TestResource {

		@JsonApiId
		private String id;

		@JsonApiRelation(lookUp = LookupIncludeBehavior.AUTOMATICALLY_WHEN_NULL, opposite = "parent",
				repositoryBehavior = RelationshipRepositoryBehavior.FORWARD_OPPOSITE)
		private OneNestedResource oneNested;

		@JsonApiRelation(lookUp = LookupIncludeBehavior.AUTOMATICALLY_WHEN_NULL, opposite = "parent",
				repositoryBehavior = RelationshipRepositoryBehavior.FORWARD_OPPOSITE)
		private List<ManyNestedResource> manyNested;

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public OneNestedResource getOneNested() {
			return oneNested;
		}

		public void setOneNested(OneNestedResource oneNested) {
			this.oneNested = oneNested;
		}

		public List<ManyNestedResource> getManyNested() {
			return manyNested;
		}

		public void setManyNested(List<ManyNestedResource> manyNested) {
			this.manyNested = manyNested;
		}
	}

	@JsonSerialize(using = ToStringSerializer.class)
	public static class ManyNestedId implements Serializable {

		@JsonApiId
		private String id;

		@JsonApiRelationId
		private String parentId;

		public ManyNestedId() {

		}

		public ManyNestedId(String idString) {
			String[] elements = idString.split("\\-");
			parentId = elements[0];
			id = elements[1];
		}

		public ManyNestedId(String parentId, String id) {
			this.parentId = parentId;
			this.id = id;
		}

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

		public int hashCode() {
			return toString().hashCode();
		}

		public boolean equals(Object object) {
			return object instanceof ManyNestedId && object.toString().equals(toString());
		}

		public String toString() {
			return parentId + "-" + id;
		}
	}

	@JsonSerialize(using = ToStringSerializer.class)
	public static class ManyGrandchildrenId implements Serializable {

		@JsonApiId
		private String id;

		@JsonApiRelationId
		private ManyNestedId parentId;

		public ManyGrandchildrenId() {

		}

		public ManyGrandchildrenId(String idString) {
			String[] elements = idString.split("\\-");
			parentId = new ManyNestedId(elements[0], elements[1]);
			id = elements[2];
		}

		public ManyGrandchildrenId(ManyNestedId parentId, String id) {
			this.parentId = parentId;
			this.id = id;
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public ManyNestedId getParentId() {
			return parentId;
		}

		public void setParentId(ManyNestedId parentId) {
			this.parentId = parentId;
		}

		public int hashCode() {
			return toString().hashCode();
		}

		public boolean equals(Object object) {
			return object instanceof ManyGrandchildrenId && object.toString().equals(toString());
		}

		public String toString() {
			return parentId + "-" + id;
		}
	}


	@JsonApiResource(type = "oneNested", nested = true)
	public static class OneNestedResource {

		@JsonApiId
		@JsonApiRelationId
		private String parentId;

		private String value;

		@JsonApiRelation(lookUp = LookupIncludeBehavior.AUTOMATICALLY_WHEN_NULL,
				repositoryBehavior = RelationshipRepositoryBehavior.FORWARD_OWNER)
		private RelatedResource related;

		@JsonApiRelation(opposite = "oneNested", lookUp = LookupIncludeBehavior.AUTOMATICALLY_WHEN_NULL,
				repositoryBehavior = RelationshipRepositoryBehavior.FORWARD_OWNER, idField = "parentId")
		private TestResource parent;

		@JsonApiRelation(lookUp = LookupIncludeBehavior.AUTOMATICALLY_WHEN_NULL, opposite = "parent",
				repositoryBehavior = RelationshipRepositoryBehavior.FORWARD_OPPOSITE)
		private OneGrandchildResource oneGrandchild;

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

		public String getParentId() {
			return parentId;
		}

		public void setParentId(String parentId) {
			this.parentId = parentId;
		}

		public RelatedResource getRelated() {
			return related;
		}

		public void setRelated(RelatedResource related) {
			this.related = related;
		}

		public OneGrandchildResource getOneGrandchild() {
			return oneGrandchild;
		}

		public void setOneGrandchild(OneGrandchildResource oneGrandchild) {
			this.oneGrandchild = oneGrandchild;
		}

	}

	@JsonApiResource(type = "oneGrandchild", nested = true)
	public static class OneGrandchildResource {

		@JsonApiId
		@JsonApiRelationId
		private String parentId;

		private String value;

		@JsonApiRelation(opposite = "oneGrandchild", lookUp = LookupIncludeBehavior.AUTOMATICALLY_WHEN_NULL,
				repositoryBehavior = RelationshipRepositoryBehavior.FORWARD_OWNER, idField = "parentId")
		private OneNestedResource parent;

		public OneNestedResource getParent() {
			return parent;
		}

		public void setParent(OneNestedResource parent) {
			this.parent = parent;
		}

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}

		public String getParentId() {
			return parentId;
		}

		public void setParentId(String parentId) {
			this.parentId = parentId;
		}
	}

	@JsonApiResource(type = "manyNested")
	public static class ManyNestedResource {

		@JsonApiId
		private ManyNestedId id;

		private String value;

		@JsonApiRelationId
		private String relatedId;

		@JsonApiRelation(lookUp = LookupIncludeBehavior.AUTOMATICALLY_WHEN_NULL,
				repositoryBehavior = RelationshipRepositoryBehavior.FORWARD_OWNER)
		private RelatedResource related;

		@JsonApiRelation(opposite = "manyNested", lookUp = LookupIncludeBehavior.AUTOMATICALLY_WHEN_NULL,
				repositoryBehavior = RelationshipRepositoryBehavior.FORWARD_OWNER)
		private TestResource parent;

		@JsonApiRelation(lookUp = LookupIncludeBehavior.AUTOMATICALLY_WHEN_NULL, opposite = "parent",
				repositoryBehavior = RelationshipRepositoryBehavior.FORWARD_OPPOSITE)
		private List<ManyGrandchildrenResource> manyGrandchildren;

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

		public List<ManyGrandchildrenResource> getManyGrandchildren() {
			return manyGrandchildren;
		}

		public void setManyGrandchildren(List<ManyGrandchildrenResource> manyGrandchildren) {
			this.manyGrandchildren = manyGrandchildren;
		}
	}

	@JsonApiResource(type = "manyGrandchildren")
	public static class ManyGrandchildrenResource {

		@JsonApiId
		private ManyGrandchildrenId id;

		private String value;

		@JsonApiRelation(opposite = "manyGrandchildren", lookUp = LookupIncludeBehavior.AUTOMATICALLY_WHEN_NULL,
				repositoryBehavior = RelationshipRepositoryBehavior.FORWARD_OWNER)
		private ManyNestedResource parent;

		public ManyGrandchildrenId getId() {
			return id;
		}

		public void setId(ManyGrandchildrenId id) {
			this.id = id;
		}

		public ManyNestedResource getParent() {
			return parent;
		}

		public void setParent(ManyNestedResource parent) {
			this.parent = parent;
		}

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
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

		public TestRepository() {
			super(TestResource.class);
		}
	}

	public static class ManyNestedRepository extends InMemoryResourceRepository<ManyNestedResource, ManyNestedId> {

		public ManyNestedRepository() {
			super(ManyNestedResource.class);
		}
	}


	public static class OneNestedRepository extends InMemoryResourceRepository<OneNestedResource, String> {

		public OneNestedRepository() {
			super(OneNestedResource.class);
		}
	}

	public static class OneGrandchildRepository extends InMemoryResourceRepository<OneGrandchildResource, String> {

		public OneGrandchildRepository() {
			super(OneGrandchildResource.class);
		}
	}

	public static class ManyGrandchildrenRepository extends InMemoryResourceRepository<ManyGrandchildrenResource, ManyGrandchildrenId> {

		public ManyGrandchildrenRepository() {
			super(ManyGrandchildrenResource.class);
		}
	}

	public static class RelatedRepository extends InMemoryResourceRepository<RelatedResource, String> {

		protected RelatedRepository() {
			super(RelatedResource.class);
		}
	}

	public static class RelationshipRepository implements io.crnk.core.repository.RelationshipRepository {

		@Override
		public Class getSourceResourceClass() {
			return ManyNestedResource.class;
		}

		@Override
		public Class getTargetResourceClass() {
			return RelatedResource.class;
		}

		@Override
		public void setRelation(Object source, Object targetId, String fieldName) {

		}

		@Override
		public void setRelations(Object source, Collection targetIds, String fieldName) {

		}

		@Override
		public void addRelations(Object source, Collection targetIds, String fieldName) {

		}

		@Override
		public void removeRelations(Object source, Collection targetIds, String fieldName) {

		}

		@Override
		public Object findOneTarget(Object sourceId, String fieldName, QuerySpec querySpec) {
			RelatedResource related = new RelatedResource();
			related.setId("related1");
			return related;
		}

		@Override
		public ResourceList findManyTargets(Object sourceId, String fieldName, QuerySpec querySpec) {
			return null;
		}
	}
}
