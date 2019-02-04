package io.crnk.jpa.integration;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import com.google.common.collect.Sets;
import io.crnk.client.internal.proxy.ObjectProxy;
import io.crnk.core.queryspec.FilterOperator;
import io.crnk.core.queryspec.FilterSpec;
import io.crnk.core.queryspec.PathSpec;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.RelationshipRepositoryV2;
import io.crnk.core.repository.ResourceRepositoryV2;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.jpa.AbstractJpaJerseyTest;
import io.crnk.jpa.JpaModule;
import io.crnk.jpa.model.ManyToManyOppositeEntity;
import io.crnk.jpa.model.ManyToManyTestEntity;
import io.crnk.jpa.model.OneToOneOppositeEntity;
import io.crnk.jpa.model.OneToOneTestEntity;
import io.crnk.jpa.model.OtherRelatedEntity;
import io.crnk.jpa.model.RelatedEntity;
import io.crnk.jpa.model.TestEntity;
import io.crnk.jpa.query.criteria.JpaCriteriaQueryFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class JpaRelationshipIntTest extends AbstractJpaJerseyTest {

	private ResourceRepositoryV2<TestEntity, Long> testRepo;

	private SessionFactory sessionFactory;

	@Override
	@Before
	public void setup() {
		super.setup();
		testRepo = client.getRepositoryForType(TestEntity.class);

		sessionFactory = context.getBean(SessionFactory.class);
	}

	@Override
	protected void setupModule(JpaModule module, boolean server) {
		if (server) {
			module.setQueryFactory(JpaCriteriaQueryFactory.newInstance());
		}
	}


	@Test
	public void testOneToOneUniDirectionalResourceUpdate() {
		testOneToOneUniDirectional(false);
	}

	@Test
	public void testOneToOneUniDirectionalRelationshipUpdate() {
		testOneToOneUniDirectional(true);
	}

	private void testOneToOneUniDirectional(boolean relationship) {
		int n = 10;
		ResourceRepositoryV2<OneToOneTestEntity, Serializable> testRepo = client.getRepositoryForType(OneToOneTestEntity.class);
		ResourceRepositoryV2<RelatedEntity, Serializable> otherRepo = client.getRepositoryForType(RelatedEntity.class);
		RelationshipRepositoryV2 relRepo = client.getRepositoryForType(OneToOneTestEntity.class, RelatedEntity.class);

		for (int i = 0; i < n; i++) {
			RelatedEntity related = new RelatedEntity();
			related.setId(12L + i);
			otherRepo.create(related);

			OneToOneTestEntity test = new OneToOneTestEntity();
			test.setId(11L + i);
			if (!relationship) {
				test.setOneRelatedValue(related);
			}
			testRepo.create(test);

			if (relationship) {
				relRepo.setRelation(test, related.getId(), "oneRelatedValue");
			}
		}

		Statistics stats = sessionFactory.getStatistics();
		stats.clear();

		QuerySpec querySpec = new QuerySpec(OneToOneTestEntity.class);
		querySpec.includeRelation(Arrays.asList("oneRelatedValue"));
		ResourceList<OneToOneTestEntity> list = testRepo.findAll(querySpec);
		Assert.assertEquals(10, list.size());
		OneToOneTestEntity testCopy = list.get(0);
		Assert.assertNotNull(testCopy.getOneRelatedValue());
		Assert.assertEquals(12L, testCopy.getOneRelatedValue().getId().longValue());

		// verify no lazy loading and n+1 issues
		Assert.assertEquals(0, stats.getEntityFetchCount());
		Assert.assertEquals(2, stats.getQueryExecutionCount());
		Assert.assertEquals(0, stats.getCollectionFetchCount());
	}

	@Test
	public void testOneToOneBiDirectionalResourceUpdate() {
		testOneToOneBiDirectional(false, false);
	}

	@Test
	public void testOneToOneBiDirectionalRelationshipUpdate() {
		testOneToOneBiDirectional(false, true);
	}

	@Test
	public void testOneToOneBiDirectionalOppositeRelationshipUpdate() {
		testOneToOneBiDirectional(true, true);
	}

	private void testOneToOneBiDirectional(boolean oppositeUpdate, boolean relationship) {
		int n = 10;
		ResourceRepositoryV2<OneToOneTestEntity, Serializable> testRepo = client.getRepositoryForType(OneToOneTestEntity.class);
		ResourceRepositoryV2<OneToOneOppositeEntity, Serializable> otherRepo = client.getRepositoryForType(OneToOneOppositeEntity.class);

		for (int i = 0; i < n; i++) {
			OneToOneOppositeEntity opposite = new OneToOneOppositeEntity();
			opposite.setId(12L + i);
			otherRepo.create(opposite);

			OneToOneTestEntity test = new OneToOneTestEntity();
			test.setId(11L + i);
			if (!relationship && !oppositeUpdate) {
				test.setOppositeValue(opposite);
			}
			testRepo.create(test);

			if (relationship && oppositeUpdate) {
				RelationshipRepositoryV2 relRepo = client.getRepositoryForType(OneToOneOppositeEntity.class, OneToOneTestEntity.class);
				relRepo.setRelation(opposite, test.getId(), "test");
			}
			else if (relationship) {
				RelationshipRepositoryV2 relRepo = client.getRepositoryForType(OneToOneTestEntity.class, OneToOneOppositeEntity.class);
				relRepo.setRelation(test, opposite.getId(), "oppositeValue");
			}
		}

		Statistics stats = sessionFactory.getStatistics();
		stats.clear();

		QuerySpec querySpec = new QuerySpec(OneToOneTestEntity.class);
		querySpec.includeRelation(Arrays.asList("oppositeValue"));
		ResourceList<OneToOneTestEntity> list = testRepo.findAll(querySpec);
		Assert.assertEquals(10, list.size());
		OneToOneTestEntity testCopy = list.get(0);
		Assert.assertNotNull(testCopy.getOppositeValue());
		Assert.assertEquals(12L, testCopy.getOppositeValue().getId().longValue());

		// verify no lazy loading and n+1 issues
		Assert.assertEquals(0, stats.getEntityFetchCount());
		Assert.assertEquals(2, stats.getQueryExecutionCount());
		Assert.assertEquals(0, stats.getCollectionFetchCount());
	}


	@Test
	public void testManyToManyResourceUpdate() {
		testManyToMany(false, false);
	}

	@Test
	public void testManyToManyRelationshipUpdate() {
		testManyToMany(false, true);
	}

	@Test
	public void testManyToManyOppositeRelationshipUpdate() {
		testManyToMany(true, true);
	}

	public void testManyToMany(boolean opposite, boolean relationship) {
		ResourceRepositoryV2<ManyToManyTestEntity, Serializable> testRepo =
				client.getRepositoryForType(ManyToManyTestEntity.class);
		ResourceRepositoryV2<ManyToManyOppositeEntity, Serializable> relatedRepo =
				client.getRepositoryForType(ManyToManyOppositeEntity.class);

		for (int i = 0; i < 10; i++) {
			ManyToManyOppositeEntity related = new ManyToManyOppositeEntity();
			related.setId(i + 12L);

			ManyToManyTestEntity test = new ManyToManyTestEntity();
			test.setId(i + 11L);

			if (opposite && relationship) {
				testRepo.create(test);
				relatedRepo.create(related);

				RelationshipRepositoryV2 relRepo = client.getRepositoryForType(ManyToManyOppositeEntity.class, ManyToManyTestEntity.class);
				relRepo.addRelations(related, Arrays.asList(test.getId()), "tests");
			}
			else if (relationship) {
				testRepo.create(test);
				relatedRepo.create(related);

				RelationshipRepositoryV2 relRepo = client.getRepositoryForType(ManyToManyTestEntity.class, ManyToManyOppositeEntity.class);
				relRepo.addRelations(test, Arrays.asList(related.getId()), "opposites");
			}
			else {
				test.setOpposites(Sets.newHashSet(related));
				relatedRepo.create(related);
				testRepo.create(test);
			}
		}

		Statistics stats = sessionFactory.getStatistics();
		stats.clear();

		QuerySpec querySpec = new QuerySpec(ManyToManyTestEntity.class);
		querySpec.includeRelation(Arrays.asList("opposites"));
		ResourceList<ManyToManyTestEntity> list = testRepo.findAll(querySpec);
		Assert.assertEquals(10, list.size());
		for (int i = 0; i < 10; i++) {
			ManyToManyTestEntity testCopy = list.get(i);
			Assert.assertEquals(1, testCopy.getOpposites().size());
		}

		// verify no lazy loading and n+1 issues
		Assert.assertEquals(0, stats.getEntityFetchCount());
		Assert.assertEquals(2, stats.getQueryExecutionCount());
		Assert.assertEquals(0, stats.getCollectionFetchCount());
	}


	@Test
	public void testIncludeNested() {
		addTestWithManyRelations();

		QuerySpec querySpec = new QuerySpec(TestEntity.class);
		querySpec.includeRelation(Arrays.asList(TestEntity.ATTR_manyRelatedValues, RelatedEntity.ATTR_otherEntity));
		List<TestEntity> list = testRepo.findAll(querySpec);

		Assert.assertEquals(1, list.size());
		TestEntity testEntity = list.get(0);

		List<RelatedEntity> manyRelatedValues = testEntity.getManyRelatedValues();
		Assert.assertNotNull(manyRelatedValues);
		Assert.assertEquals(5, manyRelatedValues.size());

		// three out of five have a relationship defined
		long n = manyRelatedValues.stream().filter(it -> it.getOtherEntity() != null).count();
		Assert.assertEquals(3, n);
	}

	@Test
	public void testFindOneTargetWithNullResult() {
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
	public void testLazyManyRelation() {
		addTestWithManyRelations();

		QuerySpec querySpec = new QuerySpec(TestEntity.class);
		List<TestEntity> list = testRepo.findAll(querySpec);

		Assert.assertEquals(1, list.size());
		TestEntity testEntity = list.get(0);

		List<RelatedEntity> manyRelatedValues = testEntity.getManyRelatedValues();
		Assert.assertNotNull(manyRelatedValues);

		ObjectProxy proxy = (ObjectProxy) manyRelatedValues;
		Assert.assertFalse(proxy.isLoaded());
		Assert.assertEquals(5, manyRelatedValues.size());

		for (RelatedEntity relatedEntity : manyRelatedValues) {
			Assert.assertNotNull(relatedEntity.getStringValue());
		}
	}

	@Test
	public void testIncludeAndFilterManyRelations() {
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
	public void testIncludeManyRelations() {
		addTestWithManyRelations(10);

		Statistics stats = sessionFactory.getStatistics();
		stats.clear();

		QuerySpec querySpec = new QuerySpec(TestEntity.class);
		querySpec.includeRelation(Arrays.asList(TestEntity.ATTR_manyRelatedValues));
		List<TestEntity> list = testRepo.findAll(querySpec);

		Assert.assertEquals(10, list.size());
		TestEntity testEntity = list.get(0);

		List<RelatedEntity> manyRelatedValues = testEntity.getManyRelatedValues();
		Assert.assertNotNull(manyRelatedValues);
		Assert.assertEquals(5, manyRelatedValues.size());

		Assert.assertEquals(0, stats.getEntityFetchCount());
		Assert.assertEquals(3, stats.getQueryExecutionCount());
		// TODO issue with map eager loading:
		// Assert.assertEquals(1, stats.getCollectionFetchCount());
	}


	@Test
	@Ignore
	// TODO bidirectionality not properly handled, see
	// ResourceUpsert should make use of relationship repositories #130
	public void testAddManyRelationWithResourceSave() {
		testAddManyRelation(true);
	}

	private void testAddManyRelation(boolean onSave) {
		ResourceRepositoryV2<RelatedEntity, Long> relatedRepo = client.getRepositoryForType(RelatedEntity.class);
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
	public void testIncludeOneRelations() {
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
	public void testIncludeNoRelations() {
		addTestWithOneRelation();

		List<TestEntity> list = testRepo.findAll(new QuerySpec(TestEntity.class));
		Assert.assertEquals(1, list.size());
		for (TestEntity test : list) {
			// in the future we may get proxies here
			Assert.assertNull(test.getOneRelatedValue());
		}
	}


	@Test
	public void testFindOneTarget() {
		TestEntity test = addTestWithOneRelation();

		RelationshipRepositoryV2<TestEntity, Serializable, RelatedEntity, Serializable> relRepo = client
				.getRepositoryForType(TestEntity.class, RelatedEntity.class);

		RelatedEntity related =
				relRepo.findOneTarget(test.getId(), TestEntity.ATTR_oneRelatedValue, new QuerySpec(RelatedEntity.class));
		Assert.assertNotNull(related);
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

	private TestEntity addTestWithOneRelation() {
		ResourceRepositoryV2<RelatedEntity, Long> relatedRepo = client.getRepositoryForType(RelatedEntity.class);
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

	@Test
	public void testAddManyRelationWithRelationshipRepository() {
		testAddManyRelation(false);
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


	private TestEntity addTestWithManyRelations() {
		return addTestWithManyRelations(1);
	}

	private TestEntity addTestWithManyRelations(int n) {
		TestEntity test = null;
		for (int j = 0; j < n; j++) {
			int offset = j * 1000;
			ResourceRepositoryV2<OtherRelatedEntity, Long> otherRepo = client
					.getRepositoryForType(OtherRelatedEntity.class);
			ResourceRepositoryV2<RelatedEntity, Long> relatedRepo = client.getRepositoryForType(RelatedEntity.class);
			RelationshipRepositoryV2<TestEntity, Long, RelatedEntity, Long> relRepo = client
					.getRepositoryForType(TestEntity.class, RelatedEntity.class);
			RelationshipRepositoryV2<RelatedEntity, Long, OtherRelatedEntity, Long> otherRelRepo = client
					.getRepositoryForType(RelatedEntity.class, OtherRelatedEntity.class);

			test = new TestEntity();
			test.setId(2L + offset);
			test.setStringValue("test");
			testRepo.create(test);

			RelatedEntity related1 = new RelatedEntity();
			related1.setId(101L + offset);
			related1.setStringValue("related1");
			relatedRepo.create(related1);

			RelatedEntity related2 = new RelatedEntity();
			related2.setId(102L + offset);
			related2.setStringValue("related2");
			relatedRepo.create(related2);

			RelatedEntity related4 = new RelatedEntity();
			related4.setId(104L + offset);
			related4.setStringValue("related4");
			relatedRepo.create(related4);

			RelatedEntity related5 = new RelatedEntity();
			related5.setId(105L + offset);
			related5.setStringValue("related5");
			relatedRepo.create(related5);

			RelatedEntity related3 = new RelatedEntity();
			related3.setId(103L + offset);
			related3.setStringValue("related3");
			relatedRepo.create(related3);


			OtherRelatedEntity other1 = new OtherRelatedEntity();
			other1.setId(101L + offset);
			other1.setStringValue("related1");
			otherRepo.create(other1);

			OtherRelatedEntity other2 = new OtherRelatedEntity();
			other2.setId(102L + offset);
			other2.setStringValue("related2");
			otherRepo.create(other2);

			OtherRelatedEntity other3 = new OtherRelatedEntity();
			other3.setId(103L + offset);
			other3.setStringValue("related3");
			otherRepo.create(other3);

			List<Long> relatedIds = Arrays.asList(related1.getId(), related2.getId(), related3.getId(), related4.getId(), related5.getId());
			relRepo.addRelations(test, relatedIds, TestEntity.ATTR_manyRelatedValues);
			otherRelRepo.setRelation(related1, other1.getId(), RelatedEntity.ATTR_otherEntity);
			otherRelRepo.setRelation(related2, other2.getId(), RelatedEntity.ATTR_otherEntity);
			otherRelRepo.setRelation(related3, other3.getId(), RelatedEntity.ATTR_otherEntity);


		}
		return test;
	}


	@Test
	public void testFilterByManyJoin() {
		ResourceRepositoryV2<TestEntity, Long> testRepo = client.getRepositoryForType(TestEntity.class);
		ResourceRepositoryV2<RelatedEntity, Long> relatedRepo = client.getRepositoryForType(RelatedEntity.class);

		TestEntity test = new TestEntity();
		test.setId(1L);
		test.setStringValue("test");
		testRepo.create(test);

		RelatedEntity related0 = new RelatedEntity();
		related0.setId(0L);
		related0.setStringValue("project0");
		related0.setTestEntity(test);
		relatedRepo.create(related0);

		RelatedEntity related1 = new RelatedEntity();
		related1.setId(1L);
		related1.setStringValue("project1");
		related0.setTestEntity(test);
		relatedRepo.create(related1);

		QuerySpec querySpec0 = new QuerySpec(TestEntity.class);
		querySpec0.addFilter(PathSpec.of(TestEntity.ATTR_manyRelatedValues, RelatedEntity.ATTR_stringValue).filter(FilterOperator.EQ, "project0"));
		Assert.assertEquals(1, testRepo.findAll(querySpec0).size());

		QuerySpec querySpec2 = new QuerySpec(TestEntity.class);
		querySpec2.addFilter(PathSpec.of(TestEntity.ATTR_manyRelatedValues, RelatedEntity.ATTR_stringValue).filter(FilterOperator.EQ, "project1"));
		Assert.assertEquals(0, testRepo.findAll(querySpec2).size());
	}
}
