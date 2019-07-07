package io.crnk.data.jpa.integration;

import io.crnk.core.resource.meta.JsonLinksInformation;
import io.crnk.core.resource.meta.JsonMetaInformation;
import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.core.queryspec.FilterOperator;
import io.crnk.core.queryspec.FilterSpec;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.RelationshipRepository;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.data.jpa.AbstractJpaJerseyTest;
import io.crnk.data.jpa.model.OverrideIdTestEntity;
import io.crnk.data.jpa.model.TestEmbeddedIdEntity;
import io.crnk.data.jpa.model.TestSubclassWithSuperclassPk;
import io.crnk.data.jpa.model.CustomTypeTestEntity;
import io.crnk.data.jpa.model.RelatedEntity;
import io.crnk.data.jpa.model.TestEntity;
import io.crnk.data.jpa.model.TestIdEmbeddable;
import io.crnk.data.jpa.model.TestMappedSuperclassWithPk;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public class JpaQuerySpecIntTest extends AbstractJpaJerseyTest {

    private ResourceRepository<TestEntity, Long> testRepo;

    @Override
    @Before
    public void setup() {
        super.setup();
        testRepo = client.getRepositoryForType(TestEntity.class);
    }

    @Test
    public void testIncludeEmptyRelations() {
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
    public void testUpdate() {
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
    public void testMappedSuperTypeWithPkOnSuperType() {
        ResourceRepository<TestSubclassWithSuperclassPk, Serializable> repo =
                client.getRepositoryForType(TestSubclassWithSuperclassPk.class);
        ResourceRepository<RelatedEntity, Serializable> relatedRepo = client.getRepositoryForType(RelatedEntity.class);

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
    public void testFilterByNull() {
        TestEntity entity = new TestEntity();
        entity.setId(345345L);
        entity.setStringValue(null);
        testRepo.create(entity);

        QuerySpec querySpec = new QuerySpec(TestEntity.class);
        querySpec.addFilter(new FilterSpec(Arrays.asList(TestEntity.ATTR_stringValue), FilterOperator.EQ, null));
        List<TestEntity> list = testRepo.findAll(querySpec);
        Assert.assertEquals(1, list.size());
        TestEntity testEntity = list.get(0);
        Assert.assertEquals(345345L, testEntity.getId().longValue());


        querySpec = new QuerySpec(TestEntity.class);
        querySpec.addFilter(new FilterSpec(Arrays.asList(TestEntity.ATTR_stringValue), FilterOperator.NEQ, null));
        list = testRepo.findAll(querySpec);
        Assert.assertEquals(0, list.size());
    }


    @Test
    public void testMappedSuperTypeWithPkOnSubclass() {
        ResourceRepository<RelatedEntity, Serializable> relatedRepo = client.getRepositoryForType(RelatedEntity.class);

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

        ResourceRepository<RelatedEntity, Serializable> relatedRepo = client.getRepositoryForType(RelatedEntity.class);
        RelationshipRepository<TestEntity, Long, RelatedEntity, Long> relRepo = client
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
        Assert.assertEquals(baseUri + "test/1/manyRelatedValues?page[limit]=2",
                links.asJsonNode().get("first").asText());
        Assert.assertEquals(baseUri + "test/1/manyRelatedValues?page[limit]=2&page[offset]=4",
                links.asJsonNode().get("last").asText());
        Assert.assertEquals(baseUri + "test/1/manyRelatedValues?page[limit]=2",
                links.asJsonNode().get("prev").asText());
        Assert.assertEquals(baseUri + "test/1/manyRelatedValues?page[limit]=2&page[offset]=4",
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


    @Test
    public void testEagerOneRelation() {
        ResourceRepository<RelatedEntity, Serializable> relatedRepo = client.getRepositoryForType(RelatedEntity.class);
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
    public void testEmbeddableIds() {
        ResourceRepository<TestEmbeddedIdEntity, Serializable> rep = client
                .getRepositoryForType(TestEmbeddedIdEntity.class);

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


    private TestEntity addTest() {
        TestEntity test = new TestEntity();
        test.setId(2L);
        test.setStringValue("test");
        testRepo.create(test);
        return test;
    }

    @Test
    public void testCustomType() {
        ResourceRepository<CustomTypeTestEntity, Serializable> repo =
                client.getRepositoryForType(CustomTypeTestEntity.class);

        CustomTypeTestEntity.CustomType customValue = new CustomTypeTestEntity.CustomType();
        customValue.setValue("test");

        CustomTypeTestEntity entity = new CustomTypeTestEntity();
        entity.setId(13L);
        entity.setValue(customValue);
        repo.create(entity);

        QuerySpec querySpec = new QuerySpec(CustomTypeTestEntity.class);
        List<CustomTypeTestEntity> list = repo.findAll(querySpec);
        Assert.assertEquals(1, list.size());
        Assert.assertEquals("test", list.get(0).getValue().getValue());
    }

    @Test
    public void testOverridenPrimaryKey() {
        ResourceRepository<OverrideIdTestEntity, Serializable> repo =
                client.getRepositoryForType(OverrideIdTestEntity.class);

        OverrideIdTestEntity entity = new OverrideIdTestEntity();
        entity.setId(13L);
        entity.setPk(42L);
        OverrideIdTestEntity createdEntity = repo.create(entity);
        checkResource(createdEntity);

        QuerySpec querySpec = new QuerySpec(OverrideIdTestEntity.class);
        List<OverrideIdTestEntity> list = repo.findAll(querySpec);
        Assert.assertEquals(1, list.size());
        checkResource(list.get(0));

        OverrideIdTestEntity findOneEntity = repo.findOne(13L, querySpec);
        checkResource(findOneEntity);

        createdEntity.setValue("newValue");
        OverrideIdTestEntity savedEntity = repo.save(createdEntity);
        checkResource(entity);
        Assert.assertEquals("newValue", savedEntity.getValue());

        repo.delete(entity.getId());
        list = repo.findAll(querySpec);
        Assert.assertEquals(0, list.size());
    }

    private void checkResource(OverrideIdTestEntity entity) {
        Assert.assertEquals(13L, entity.getId().longValue());
        Assert.assertEquals(42L, entity.getPk().longValue());
    }
}
