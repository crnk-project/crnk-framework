package io.crnk.jpa;

import io.crnk.client.legacy.ResourceRepositoryStub;
import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.jpa.model.*;
import io.crnk.legacy.queryParams.QueryParams;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.OptimisticLockException;
import java.io.Serializable;
import java.util.*;

public class JpaQueryParamsEndToEndTest extends AbstractJpaJerseyTest {

	protected ResourceRepositoryStub<TestEntity, Long> testRepo;

	@Override
	@Before
	public void setup() {
		super.setup();
		testRepo = client.getQueryParamsRepository(TestEntity.class);
	}

	@Test
	public void testIncludeRelations() throws InstantiationException, IllegalAccessException {
		addTestWithOneRelation();

		List<TestEntity> list = findAll("include[test]", TestEntity.ATTR_oneRelatedValue);
		Assert.assertEquals(1, list.size());
		for (TestEntity test : list) {
			Assert.assertNotNull(test.getOneRelatedValue());
		}
	}

	@Test
	public void testIncludeNoRelations() throws InstantiationException, IllegalAccessException {
		addTestWithOneRelation();

		List<TestEntity> list = testRepo.findAll(new QueryParams());
		Assert.assertEquals(1, list.size());
		for (TestEntity test : list) {
			// in the future we may get proxies here
			Assert.assertNull(test.getOneRelatedValue());
		}
	}

	@Test
	public void testFindEmpty() {
		List<TestEntity> list = testRepo.findAll(new QueryParams());
		Assert.assertTrue(list.isEmpty());
	}

	@Test(expected = ResourceNotFoundException.class)
	public void testNotFound() {
		testRepo.findOne(1L, new QueryParams());
	}

	@Test
	public void testSaveAndFind() {
		TestEntity task = new TestEntity();
		task.setId(1L);
		task.setStringValue("test");
		testRepo.create(task);

		// check retrievable with findAll
		List<TestEntity> list = testRepo.findAll(new QueryParams());
		Assert.assertEquals(1, list.size());
		TestEntity savedTask = list.get(0);
		Assert.assertEquals(task.getId(), savedTask.getId());
		Assert.assertEquals(task.getStringValue(), savedTask.getStringValue());

		// check retrievable with findAll(ids)
		list = testRepo.findAll(Arrays.asList(1L), new QueryParams());
		Assert.assertEquals(1, list.size());
		savedTask = list.get(0);
		Assert.assertEquals(task.getId(), savedTask.getId());
		Assert.assertEquals(task.getStringValue(), savedTask.getStringValue());

		// check retrievable with findOne
		savedTask = testRepo.findOne(1L, new QueryParams());
		Assert.assertEquals(task.getId(), savedTask.getId());
		Assert.assertEquals(task.getStringValue(), savedTask.getStringValue());
	}

	@Test
	public void testOptimisticLocking() {
		ResourceRepositoryStub<VersionedEntity, Serializable> repo = client.getQueryParamsRepository(VersionedEntity.class);
		VersionedEntity entity = new VersionedEntity();
		entity.setId(1L);
		entity.setLongValue(13L);
		VersionedEntity saved = repo.create(entity);
		Assert.assertEquals(0, saved.getVersion());

		saved.setLongValue(14L);
		saved = repo.save(saved);
		Assert.assertEquals(1, saved.getVersion());

		saved.setLongValue(15L);
		saved = repo.save(saved);
		Assert.assertEquals(2, saved.getVersion());

		saved.setLongValue(16L);
		saved.setVersion(saved.getVersion() - 1);
		try {
			saved = repo.save(saved);
			Assert.fail();
		} catch (OptimisticLockException e) {
			// ok
		}

		VersionedEntity persisted = repo.findOne(1L, new QueryParams());
		Assert.assertEquals(2, persisted.getVersion());
		Assert.assertEquals(15L, persisted.getLongValue());
	}

	@Test
	public void testDelete() {
		TestEntity test = new TestEntity();
		test.setId(1L);
		test.setStringValue("test");
		testRepo.create(test);

		testRepo.delete(1L);

		List<TestEntity> list = testRepo.findAll(new QueryParams());
		Assert.assertEquals(0, list.size());
	}

	private QueryParams includeOneRelatedValueParams() {
		Map<String, Set<String>> params = new HashMap<String, Set<String>>();
		addParams(params, "include[test]", TestEntity.ATTR_oneRelatedValue);
		QueryParams queryParams = queryParamsBuilder.buildQueryParams(params);
		return queryParams;
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

		TestEntity savedTest = testRepo.findOne(2L, new QueryParams());
		Assert.assertEquals(test.getId(), savedTest.getId());
		Assert.assertEquals(test.getStringValue(), savedTest.getStringValue());
		Assert.assertNull(savedTest.getOneRelatedValue());

		// TODO should @JsonApiIncludeByDefault trigger this?
		//		Assert.assertNotNull(savedTest.getEagerRelatedValue());
		//		Assert.assertEquals(1L, savedTest.getEagerRelatedValue().getId().longValue());
	}

	@Test
	public void testEmbeddableIds() throws InstantiationException, IllegalAccessException {
		ResourceRepositoryStub<TestEmbeddedIdEntity, Serializable> rep =
				client.getQueryParamsRepository(TestEmbeddedIdEntity.class);

		// add
		TestEmbeddedIdEntity entity = new TestEmbeddedIdEntity();
		entity.setId(new TestIdEmbeddable(13, "test"));
		entity.setLongValue(100L);
		rep.create(entity);

		List<TestEmbeddedIdEntity> list = rep.findAll(new QueryParams());
		Assert.assertEquals(1, list.size());
		TestEmbeddedIdEntity savedEntity = list.get(0);
		Assert.assertNotNull(savedEntity);
		Assert.assertEquals(100L, savedEntity.getLongValue());
		Assert.assertEquals(13, savedEntity.getId().getEmbIntValue().intValue());
		Assert.assertEquals("test", savedEntity.getId().getEmbStringValue());

		// update
		savedEntity.setLongValue(101L);
		rep.save(savedEntity);
		list = rep.findAll(new QueryParams());
		Assert.assertEquals(1, list.size());
		savedEntity = list.get(0);
		Assert.assertEquals(101L, savedEntity.getLongValue());

		// delete
		rep.delete(entity.getId());
		list = rep.findAll(new QueryParams());
		Assert.assertEquals(0, list.size());
	}

	private TestEntity addTestWithOneRelation() {
		ResourceRepositoryStub<RelatedEntity, Long> relatedRepo = client.getQueryParamsRepository(RelatedEntity.class);
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

	private List<TestEntity> findAll(String paramKey, String paramValue) {
		Map<String, Set<String>> params = new HashMap<String, Set<String>>();
		addParams(params, paramKey, paramValue);
		QueryParams queryParams = queryParamsBuilder.buildQueryParams(params);
		return testRepo.findAll(queryParams);
	}

	private void addParams(Map<String, Set<String>> params, String key, String value) {
		params.put(key, new HashSet<String>(Arrays.asList(value)));
	}
}
