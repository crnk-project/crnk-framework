package io.crnk.client;

import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.core.module.SimpleModule;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.InMemoryResourceRepository;
import io.crnk.core.repository.RelationshipRepositoryV2;
import io.crnk.core.repository.ResourceRepository;
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


public class InheritanceWithoutSubtypeRepositoryClientTest extends AbstractClientTest {

	private static TestRepositoryA repository = new TestRepositoryA();

	private static RelatedRepository relatedRepository = new RelatedRepository();

	private static RelationshipRepository relationshipRepository = new RelationshipRepository();

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
	protected TestApplication configure() {
		TestApplication app = new TestApplication();
		SimpleModule module = new SimpleModule("inheritance");
		module.addRepository(repository);
		module.addRepository(relatedRepository);
		module.addRepository(relationshipRepository);
		app.getFeature().addModule(module);
		return app;
	}

	@Test
	public void checkCrud() {
		// create
		TestResourceB resource = new TestResourceB();
		resource.setId("test1");
		TestResourceB createdResource = repository.create(resource);
		Assert.assertNotNull(createdResource);

		// get
		TestResourceA getResource = repository.findOne("test1", new QuerySpec(TestResourceA.class));
		Assert.assertNotNull(getResource);

		// delete
		repository.delete(resource.getId());
		try {
			repository.findOne("test1", new QuerySpec(TestResourceA.class));
			Assert.fail();
		} catch (ResourceNotFoundException e) {
			// ok
		}
	}

	@Test
	public void checkFindAllReturnsProperSubtype() {
		TestResourceB resource = new TestResourceB();
		resource.setId("test0");
		repository.create(resource);

		ResourceRepository<TestResourceA, Serializable> repository = client.getRepositoryForType(TestResourceA.class);

		ResourceList<TestResourceA> list = repository.findAll(new QuerySpec(TestResourceA.class));
		Assert.assertEquals(1, list.size());

		TestResourceA test = list.get(0);
		Assert.assertTrue(test instanceof TestResourceB);
		Assert.assertEquals("test0", test.getId());
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
