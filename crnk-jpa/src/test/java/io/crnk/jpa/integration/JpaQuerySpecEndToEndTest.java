package io.crnk.jpa.integration;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import io.crnk.client.internal.proxy.ObjectProxy;
import io.crnk.client.legacy.ResourceRepositoryStub;
import io.crnk.client.response.JsonLinksInformation;
import io.crnk.client.response.JsonMetaInformation;
import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.core.queryspec.FilterOperator;
import io.crnk.core.queryspec.FilterSpec;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.RelationshipRepositoryV2;
import io.crnk.core.repository.ResourceRepositoryV2;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.jpa.AbstractJpaJerseyTest;
import io.crnk.jpa.model.OtherRelatedEntity;
import io.crnk.jpa.model.RelatedEntity;
import io.crnk.jpa.model.TestEmbeddedIdEntity;
import io.crnk.jpa.model.TestEntity;
import io.crnk.jpa.model.TestIdEmbeddable;
import io.crnk.jpa.model.TestMappedSuperclassWithPk;
import io.crnk.jpa.model.TestSubclassWithSuperclassPk;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class JpaQuerySpecEndToEndTest extends AbstractJpaJerseyTest {

	private ResourceRepositoryV2<TestEntity, Long> testRepo;

	@Override
	@Before
	public void setup() {
		super.setup();
		testRepo = client.getQuerySpecRepository(TestEntity.class);
	}

	@Test
	public void testIncludeOneRelations() throws InstantiationException, IllegalAccessException {
		addTestWithOneRelation();

		QuerySpec querySpec = new QuerySpec(TestEntity.class);
		querySpec.includeRelation(Arrays.asList(TestEntity.ATTR_oneRelatedValue));
		List<TestEntity> list = testRepo.findAll(querySpec);

		Assert.assertEquals(1, list.size());
		for (TestEntity test : list) {
			Assert.assertNotNull(test.getOneRelatedValue());
		}
	}

	@Test
	public void testIncludeEmptyRelations() throws InstantiationException, IllegalAccessException {
		addTest();

		QuerySpec querySpec = new QuerySpec(TestEntity.class);
		querySpec.includeRelation(Arrays.asList(TestEntity.ATTR_oneRelatedValue));
		querySpec.includeRelation(Arrays.asList(TestEntity.ATTR_manyRelatedValues));
		List<TestEntity> list = testRepo.findAll(querySpec);

		Assert.assertEquals(1, list.size());
		for (TestEntity test : list) {
			Assert.assertNull(test.getOneRelatedValue());
			Assert.assertEquals(0, test.getManyRelatedValues().size());
		}
	}

	@Test
	public void testUpdate() throws InstantiationException, IllegalAccessException {
		TestEntity test = addTest();

		test.setLongValue(15);
		testRepo.save(test);

		List<TestEntity> list = testRepo.findAll(new QuerySpec(TestEntity.class));
		Assert.assertEquals(1, list.size());
		test = list.get(0);
		Assert.assertEquals(15, test.getLongValue());

		test.setLongValue(16);
		testRepo.save(test);

		list = testRepo.findAll(new QuerySpec(TestEntity.class));
		Assert.assertEquals(1, list.size());
		test = list.get(0);
		Assert.assertEquals(16, test.getLongValue());
	}

	@Test
	public void testIncludeNested() throws InstantiationException, IllegalAccessException {
		addTestWithManyRelations();

		QuerySpec querySpec = new QuerySpec(TestEntity.class);
		querySpec.includeRelation(Arrays.asList(TestEntity.ATTR_manyRelatedValues, RelatedEntity.ATTR_otherEntity));
		List<TestEntity> list = testRepo.findAll(querySpec);

		Assert.assertEquals(1, list.size());
		TestEntity testEntity = list.get(0);

		List<RelatedEntity> manyRelatedValues = testEntity.getManyRelatedValues();
		Assert.assertNotNull(manyRelatedValues);
		Assert.assertEquals(3, manyRelatedValues.size());

		for (RelatedEntity relatedEntity : manyRelatedValues) {
			Assert.assertNotNull(relatedEntity.getOtherEntity());
		}
	}

	@Test
	public void testLazyManyRelation() throws InstantiationException, IllegalAccessException {
		addTestWithManyRelations();

		QuerySpec querySpec = new QuerySpec(TestEntity.class);
		List<TestEntity> list = testRepo.findAll(querySpec);

		Assert.assertEquals(1, list.size());
		TestEntity testEntity = list.get(0);

		List<RelatedEntity> manyRelatedValues = testEntity.getManyRelatedValues();
		Assert.assertNotNull(manyRelatedValues);

		ObjectProxy proxy = (ObjectProxy) manyRelatedValues;
		Assert.assertFalse(proxy.isLoaded());
		Assert.assertEquals(3, manyRelatedValues.size());

		for (RelatedEntity relatedEntity : manyRelatedValues) {
			Assert.assertNotNull(relatedEntity.getStringValue());
		}
	}

	@Test
	public void testIncludeManyRelations() throws InstantiationException, IllegalAccessException {
		addTestWithManyRelations();

		QuerySpec querySpec = new QuerySpec(TestEntity.class);
		querySpec.includeRelation(Arrays.asList(TestEntity.ATTR_manyRelatedValues));
		List<TestEntity> list = testRepo.findAll(querySpec);

		Assert.assertEquals(1, list.size());
		TestEntity testEntity = list.get(0);

		List<RelatedEntity> manyRelatedValues = testEntity.getManyRelatedValues();
		Assert.assertNotNull(manyRelatedValues);
		Assert.assertEquals(3, manyRelatedValues.size());
	}

	@Test
	public void testMappedSuperTypeWithPkOnSuperType() throws InstantiationException, IllegalAccessException {
		ResourceRepositoryV2<TestSubclassWithSuperclassPk, Serializable> repo =
				client.getQuerySpecRepository(TestSubclassWithSuperclassPk.class);
		ResourceRepositoryV2<RelatedEntity, Serializable> relatedRepo = client.getQuerySpecRepository(RelatedEntity.class);

		RelatedEntity related = new RelatedEntity();
		related.setId(23423L);
		related.setStringValue("test");
		relatedRepo.create(related);

		TestSubclassWithSuperclassPk entity = new TestSubclassWithSuperclassPk();
		entity.setId("test");
		entity.setLongValue(12L);
		entity.setSuperRelatedValue(related);
		repo.create(entity);

		QuerySpec querySpec = new QuerySpec(TestSubclassWithSuperclassPk.class);
		querySpec.includeRelation(Arrays.asList(TestMappedSuperclassWithPk.ATTR_superRelatedValue));
		List<TestSubclassWithSuperclassPk> list = repo.findAll(querySpec);

		Assert.assertEquals(1, list.size());
		TestSubclassWithSuperclassPk testEntity = list.get(0);

		RelatedEntity superRelatedValue = testEntity.getSuperRelatedValue();
		Assert.assertNotNull(superRelatedValue);
	}

	@Test
	public void testMappedSuperTypeWithPkOnSubclass() throws InstantiationException, IllegalAccessException {
		ResourceRepositoryV2<RelatedEntity, Serializable> relatedRepo = client.getQuerySpecRepository(RelatedEntity.class);

		RelatedEntity related = new RelatedEntity();
		related.setId(23423L);
		related.setStringValue("test");
		relatedRepo.create(related);

		TestEntity entity = new TestEntity();
		entity.setId(345345L);
		entity.setLongValue(12L);
		entity.setSuperRelatedValue(related);
		testRepo.create(entity);

		QuerySpec querySpec = new QuerySpec(TestEntity.class);
		querySpec.includeRelation(Arrays.asList(TestEntity.ATTR_superRelatedValue));
		List<TestEntity> list = testRepo.findAll(querySpec);

		Assert.assertEquals(1, list.size());
		TestEntity testEntity = list.get(0);

		RelatedEntity superRelatedValue = testEntity.getSuperRelatedValue();
		Assert.assertNotNull(superRelatedValue);
	}

	@Test
	public void testIncludeAndFilterManyRelations() throws InstantiationException, IllegalAccessException {
		addTestWithManyRelations();

		QuerySpec querySpec = new QuerySpec(TestEntity.class);
		querySpec.includeRelation(Arrays.asList(TestEntity.ATTR_manyRelatedValues));
		QuerySpec relatedSpec = querySpec.getOrCreateQuerySpec(RelatedEntity.class);
		relatedSpec.addFilter(new FilterSpec(Arrays.asList(RelatedEntity.ATTR_id), FilterOperator.LT, 103L));
		List<TestEntity> list = testRepo.findAll(querySpec);

		Assert.assertEquals(1, list.size());
		TestEntity testEntity = list.get(0);

		List<RelatedEntity> manyRelatedValues = testEntity.getManyRelatedValues();
		Assert.assertNotNull(manyRelatedValues);
		Assert.assertEquals(2, manyRelatedValues.size());

		for (RelatedEntity manyRelatedValue : manyRelatedValues) {
			Assert.assertTrue(manyRelatedValue.getId() == 101L || manyRelatedValue.getId() == 102L);
		}
	}

	@Test
	public void testFindOneTargetWithNullResult() throws InstantiationException, IllegalAccessException {
		TestEntity test = new TestEntity();
		test.setId(2L);
		test.setStringValue("test");
		testRepo.create(test);

		RelationshipRepositoryV2<TestEntity, Serializable, RelatedEntity, Serializable> relRepo = client
				.getRepositoryForType(TestEntity.class, RelatedEntity.class);

		RelatedEntity related =
				relRepo.findOneTarget(test.getId(), TestEntity.ATTR_oneRelatedValue, new QuerySpec(RelatedEntity.class));
		Assert.assertNull(related);
	}

	@Test
	public void testFindOneTarget() throws InstantiationException, IllegalAccessException {
		TestEntity test = addTestWithOneRelation();

		RelationshipRepositoryV2<TestEntity, Serializable, RelatedEntity, Serializable> relRepo = client
				.getRepositoryForType(TestEntity.class, RelatedEntity.class);

		RelatedEntity related =
				relRepo.findOneTarget(test.getId(), TestEntity.ATTR_oneRelatedValue, new QuerySpec(RelatedEntity.class));
		Assert.assertNotNull(related);
	}

	@Test
	public void testAddManyRelationWithRelationshipRepository() throws InstantiationException, IllegalAccessException {
		testAddManyRelation(false);
	}

	@Test
	@Ignore
	// TODO bidirectionality not properly handled, see
	// ResourceUpsert should make use of relationship repositories #130
	public void testAddManyRelationWithResourceSave() throws InstantiationException, IllegalAccessException {
		testAddManyRelation(true);
	}

	private void testAddManyRelation(boolean onSave) throws InstantiationException, IllegalAccessException {
		ResourceRepositoryV2<RelatedEntity, Long> relatedRepo = client.getQuerySpecRepository(RelatedEntity.class);
		RelatedEntity related1 = new RelatedEntity();
		related1.setId(1L);
		related1.setStringValue("related1");
		relatedRepo.create(related1);

		RelatedEntity related2 = new RelatedEntity();
		related2.setId(2L);
		related2.setStringValue("related2");
		relatedRepo.create(related2);

		TestEntity test = new TestEntity();
		test.setId(3L);
		test.setStringValue("test");
		if (onSave) {
			test.setManyRelatedValues(Arrays.asList(related1, related2));
		}
		testRepo.create(test);

		// query relation
		RelationshipRepositoryV2<TestEntity, Long, RelatedEntity, Long> relRepo = client
				.getRepositoryForType(TestEntity.class, RelatedEntity.class);

		if (!onSave) {
			relRepo.addRelations(test, Arrays.asList(1L, 2L), TestEntity.ATTR_manyRelatedValues);
		}
		List<RelatedEntity> related =
				relRepo.findManyTargets(test.getId(), TestEntity.ATTR_manyRelatedValues, new QuerySpec(RelatedEntity.class));
		Assert.assertEquals(2, related.size());

		// query relation in opposite direction
		RelationshipRepositoryV2<RelatedEntity, Serializable, TestEntity, Serializable> backRelRepo = client
				.getRepositoryForType(RelatedEntity.class, TestEntity.class);

		test = backRelRepo.findOneTarget(2L, RelatedEntity.ATTR_testEntity, new QuerySpec(TestEntity.class));
		Assert.assertNotNull(test);
		Assert.assertEquals(3L, test.getId().longValue());
	}

	@Test
	public void testIncludeNoRelations() throws InstantiationException, IllegalAccessException {
		addTestWithOneRelation();

		List<TestEntity> list = testRepo.findAll(new QuerySpec(TestEntity.class));
		Assert.assertEquals(1, list.size());
		for (TestEntity test : list) {
			// in the future we may get proxies here
			Assert.assertNull(test.getOneRelatedValue());
		}
	}

	@Test
	public void testFindEmpty() {
		List<TestEntity> list = testRepo.findAll(new QuerySpec(TestEntity.class));
		Assert.assertTrue(list.isEmpty());
	}

	@Test(expected = ResourceNotFoundException.class)
	public void testNotFound() {
		testRepo.findOne(1L, new QuerySpec(TestEntity.class));
	}

	@Test
	public void testSaveAndFind() {
		TestEntity task = new TestEntity();
		task.setId(1L);
		task.setStringValue("test");
		testRepo.create(task);

		// check retrievable with findAll
		List<TestEntity> list = testRepo.findAll(new QuerySpec(TestEntity.class));
		Assert.assertEquals(1, list.size());
		TestEntity savedTask = list.get(0);
		Assert.assertEquals(task.getId(), savedTask.getId());
		Assert.assertEquals(task.getStringValue(), savedTask.getStringValue());

		// check retrievable with findAll(ids)
		list = testRepo.findAll(Arrays.asList(1L), new QuerySpec(TestEntity.class));
		Assert.assertEquals(1, list.size());
		savedTask = list.get(0);
		Assert.assertEquals(task.getId(), savedTask.getId());
		Assert.assertEquals(task.getStringValue(), savedTask.getStringValue());

		// check retrievable with findOne
		savedTask = testRepo.findOne(1L, new QuerySpec(TestEntity.class));
		Assert.assertEquals(task.getId(), savedTask.getId());
		Assert.assertEquals(task.getStringValue(), savedTask.getStringValue());
	}

	@Test
	public void testRootPaging() {
		for (long i = 0; i < 5; i++) {
			TestEntity task = new TestEntity();
			task.setId(i);
			task.setStringValue("test");
			testRepo.create(task);
		}

		QuerySpec querySpec = new QuerySpec(TestEntity.class);
		querySpec.setOffset(2L);
		querySpec.setLimit(2L);

		ResourceList<TestEntity> list = testRepo.findAll(querySpec);
		Assert.assertEquals(2, list.size());
		Assert.assertEquals(2, list.get(0).getId().intValue());
		Assert.assertEquals(3, list.get(1).getId().intValue());

		JsonMetaInformation meta = list.getMeta(JsonMetaInformation.class);
		JsonLinksInformation links = list.getLinks(JsonLinksInformation.class);
		Assert.assertNotNull(meta);
		Assert.assertNotNull(links);

		String baseUri = getBaseUri().toString();
		Assert.assertEquals(baseUri + "test?page[limit]=2", links.asJsonNode().get("first").asText());
		Assert.assertEquals(baseUri + "test?page[limit]=2&page[offset]=4", links.asJsonNode().get("last").asText());
		Assert.assertEquals(baseUri + "test?page[limit]=2", links.asJsonNode().get("prev").asText());
		Assert.assertEquals(baseUri + "test?page[limit]=2&page[offset]=4", links.asJsonNode().get("next").asText());
	}

	@Test
	public void testRelationPaging() {
		TestEntity test = new TestEntity();
		test.setId(1L);
		test.setStringValue("test");
		testRepo.create(test);

		ResourceRepositoryStub<RelatedEntity, Long> relatedRepo = client.getQueryParamsRepository(RelatedEntity.class);
		RelationshipRepositoryV2<TestEntity, Long, RelatedEntity, Long> relRepo = client
				.getRepositoryForType(TestEntity.class, RelatedEntity.class);

		for (long i = 0; i < 5; i++) {
			RelatedEntity related1 = new RelatedEntity();
			related1.setId(i);
			related1.setStringValue("related" + i);
			relatedRepo.create(related1);

			relRepo.addRelations(test, Arrays.asList(i), TestEntity.ATTR_manyRelatedValues);
		}

		QuerySpec querySpec = new QuerySpec(RelatedEntity.class);
		querySpec.setOffset(2L);
		querySpec.setLimit(2L);

		ResourceList<RelatedEntity> list = relRepo.findManyTargets(test.getId(), TestEntity.ATTR_manyRelatedValues, querySpec);
		Assert.assertEquals(2, list.size());
		Assert.assertEquals(2, list.get(0).getId().intValue());
		Assert.assertEquals(3, list.get(1).getId().intValue());

		JsonMetaInformation meta = list.getMeta(JsonMetaInformation.class);
		JsonLinksInformation links = list.getLinks(JsonLinksInformation.class);
		Assert.assertNotNull(meta);
		Assert.assertNotNull(links);

		String baseUri = getBaseUri().toString();
		Assert.assertEquals(baseUri + "test/1/relationships/manyRelatedValues?page[limit]=2",
				links.asJsonNode().get("first").asText());
		Assert.assertEquals(baseUri + "test/1/relationships/manyRelatedValues?page[limit]=2&page[offset]=4",
				links.asJsonNode().get("last").asText());
		Assert.assertEquals(baseUri + "test/1/relationships/manyRelatedValues?page[limit]=2",
				links.asJsonNode().get("prev").asText());
		Assert.assertEquals(baseUri + "test/1/relationships/manyRelatedValues?page[limit]=2&page[offset]=4",
				links.asJsonNode().get("next").asText());
	}

	@Test
	public void testDelete() {
		TestEntity test = new TestEntity();
		test.setId(1L);
		test.setStringValue("test");
		testRepo.create(test);

		testRepo.delete(1L);

		List<TestEntity> list = testRepo.findAll(new QuerySpec(TestEntity.class));
		Assert.assertEquals(0, list.size());
	}

	private QuerySpec includeOneRelatedValueParams() {
		QuerySpec querySpec = new QuerySpec(TestEntity.class);
		querySpec.includeRelation(Arrays.asList(TestEntity.ATTR_oneRelatedValue));
		return querySpec;
	}

	private QuerySpec includeManyRelatedValueParams() {
		QuerySpec querySpec = new QuerySpec(TestEntity.class);
		querySpec.includeRelation(Arrays.asList(TestEntity.ATTR_manyRelatedValues));
		return querySpec;
	}

	@Test
	public void testSaveOneRelation() {
		TestEntity test = addTestWithOneRelation();

		TestEntity savedTest = testRepo.findOne(2L, includeOneRelatedValueParams());
		Assert.assertEquals(test.getId(), savedTest.getId());
		Assert.assertEquals(test.getStringValue(), savedTest.getStringValue());
		Assert.assertNotNull(savedTest.getOneRelatedValue());
		Assert.assertEquals(1L, savedTest.getOneRelatedValue().getId().longValue());
	}

	@Test
	public void testEagerOneRelation() {
		ResourceRepositoryStub<RelatedEntity, Long> relatedRepo = client.getQueryParamsRepository(RelatedEntity.class);
		RelatedEntity related = new RelatedEntity();
		related.setId(1L);
		related.setStringValue("project");
		relatedRepo.create(related);

		TestEntity test = new TestEntity();
		test.setId(2L);
		test.setStringValue("test");
		test.setEagerRelatedValue(related);
		testRepo.create(test);

		TestEntity savedTest = testRepo.findOne(2L, new QuerySpec(TestEntity.class));
		Assert.assertEquals(test.getId(), savedTest.getId());
		Assert.assertEquals(test.getStringValue(), savedTest.getStringValue());
		Assert.assertNull(savedTest.getOneRelatedValue());

		// TOOD should @JsonApiIncludeByDefault trigger this?
		// Assert.assertNotNull(savedTest.getEagerRelatedValue());
		// Assert.assertEquals(1L,
		// savedTest.getEagerRelatedValue().getId().longValue());
	}

	@Test
	public void testEmbeddableIds() throws InstantiationException, IllegalAccessException {
		ResourceRepositoryV2<TestEmbeddedIdEntity, Serializable> rep = client
				.getQuerySpecRepository(TestEmbeddedIdEntity.class);

		// add
		TestEmbeddedIdEntity entity = new TestEmbeddedIdEntity();
		entity.setId(new TestIdEmbeddable(13, "test"));
		entity.setLongValue(100L);
		rep.create(entity);

		List<TestEmbeddedIdEntity> list = rep.findAll(new QuerySpec(TestEntity.class));
		Assert.assertEquals(1, list.size());
		TestEmbeddedIdEntity savedEntity = list.get(0);
		Assert.assertNotNull(savedEntity);
		Assert.assertEquals(100L, savedEntity.getLongValue());
		Assert.assertEquals(13, savedEntity.getId().getEmbIntValue().intValue());
		Assert.assertEquals("test", savedEntity.getId().getEmbStringValue());

		// update
		savedEntity.setLongValue(101L);
		rep.save(savedEntity);
		list = rep.findAll(new QuerySpec(TestEntity.class));
		Assert.assertEquals(1, list.size());
		savedEntity = list.get(0);
		Assert.assertEquals(101L, savedEntity.getLongValue());

		// delete
		rep.delete(entity.getId());
		list = rep.findAll(new QuerySpec(TestEntity.class));
		Assert.assertEquals(0, list.size());
	}

	private TestEntity addTestWithOneRelation() {
		ResourceRepositoryV2<RelatedEntity, Long> relatedRepo = client.getQuerySpecRepository(RelatedEntity.class);
		RelatedEntity related = new RelatedEntity();
		related.setId(1L);
		related.setStringValue("project");
		relatedRepo.create(related);

		TestEntity test = new TestEntity();
		test.setId(2L);
		test.setStringValue("test");
		test.setOneRelatedValue(related);
		testRepo.create(test);
		return test;
	}

	private TestEntity addTest() {
		TestEntity test = new TestEntity();
		test.setId(2L);
		test.setStringValue("test");
		testRepo.create(test);
		return test;
	}

	private TestEntity addTestWithManyRelations() {
		ResourceRepositoryV2<OtherRelatedEntity, Long> otherRepo = client
				.getRepositoryForType(OtherRelatedEntity.class);
		ResourceRepositoryV2<RelatedEntity, Long> relatedRepo = client.getQuerySpecRepository(RelatedEntity.class);
		RelationshipRepositoryV2<TestEntity, Long, RelatedEntity, Long> relRepo = client
				.getRepositoryForType(TestEntity.class, RelatedEntity.class);
		RelationshipRepositoryV2<RelatedEntity, Long, OtherRelatedEntity, Long> otherRelRepo = client
				.getRepositoryForType(RelatedEntity.class, OtherRelatedEntity.class);

		TestEntity test = new TestEntity();
		test.setId(2L);
		test.setStringValue("test");
		testRepo.create(test);

		RelatedEntity related1 = new RelatedEntity();
		related1.setId(101L);
		related1.setStringValue("related1");
		relatedRepo.create(related1);

		RelatedEntity related2 = new RelatedEntity();
		related2.setId(102L);
		related2.setStringValue("related2");
		relatedRepo.create(related2);

		RelatedEntity related3 = new RelatedEntity();
		related3.setId(103L);
		related3.setStringValue("related3");
		relatedRepo.create(related3);

		OtherRelatedEntity other1 = new OtherRelatedEntity();
		other1.setId(101L);
		other1.setStringValue("related1");
		otherRepo.create(other1);

		OtherRelatedEntity other2 = new OtherRelatedEntity();
		other2.setId(102L);
		other2.setStringValue("related2");
		otherRepo.create(other2);

		OtherRelatedEntity other3 = new OtherRelatedEntity();
		other3.setId(103L);
		other3.setStringValue("related3");
		otherRepo.create(other3);

		List<Long> relatedIds = Arrays.asList(related1.getId(), related2.getId(), related3.getId());
		relRepo.addRelations(test, relatedIds, TestEntity.ATTR_manyRelatedValues);
		otherRelRepo.setRelation(related1, other1.getId(), RelatedEntity.ATTR_otherEntity);
		otherRelRepo.setRelation(related2, other2.getId(), RelatedEntity.ATTR_otherEntity);
		otherRelRepo.setRelation(related3, other3.getId(), RelatedEntity.ATTR_otherEntity);

		return test;
	}
}
