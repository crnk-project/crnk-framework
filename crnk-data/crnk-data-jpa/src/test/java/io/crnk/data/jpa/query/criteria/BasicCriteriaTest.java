package io.crnk.data.jpa.query.criteria;

import io.crnk.core.queryspec.FilterOperator;
import io.crnk.data.jpa.internal.query.backend.criteria.JpaCriteriaQueryExecutorImpl;
import io.crnk.data.jpa.model.RelatedEntity;
import io.crnk.data.jpa.model.TestEntity;
import io.crnk.data.jpa.query.BasicQueryTestBase;
import io.crnk.data.jpa.query.JpaQuery;
import io.crnk.data.jpa.query.JpaQueryExecutor;
import io.crnk.data.jpa.query.JpaQueryFactory;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class BasicCriteriaTest extends BasicQueryTestBase {

    @Override
    protected JpaQueryFactory createQueryFactory(EntityManager em) {
        return JpaCriteriaQueryFactory.newInstance();
    }

    @Test
    @Ignore
    public void testEqualsInCollectionFilter() {
        // TODO invalid SQL generated, maybe due to use of list?
    }

    @Test
    public void testAutoDistinctOnManyRelation() {
        JpaQuery<TestEntity> builder = queryFactory.query(TestEntity.class);

        builder.addFilter(TestEntity.ATTR_manyRelatedValues + ".id", FilterOperator.EQ, Arrays.asList(1001L, 2001L));

        for (int i = 0; i < 5; i++) {
            TestEntity test = new TestEntity();
            test.setStringValue("test" + i);
            test.setId(3000 + (long) i);
            test.setLongValue(i);
            em.persist(test);

            RelatedEntity related1 = new RelatedEntity();
            related1.setId(1000L + i);
            related1.setStringValue("related1" + i);
            related1.setTestEntity(test);
            em.persist(related1);

            RelatedEntity related2 = new RelatedEntity();
            related2.setId(2000L + i);
            related2.setStringValue("related2" + i);
            related2.setTestEntity(test);
            em.persist(related2);
        }
        em.flush();

        JpaQueryExecutor<TestEntity> executor = builder.buildExecutor();
        Assert.assertEquals(1, executor.getTotalRowCount());
        List<TestEntity> relatedEntity = executor.getResultList();
        assertEquals(1L, relatedEntity.size());
        TestEntity testEntity = relatedEntity.get(0);
        Assert.assertEquals("test1", testEntity.getStringValue());
    }


    @Test
    public void testSetCached() {
        JpaQuery<TestEntity> builder = queryFactory.query(TestEntity.class);
        JpaCriteriaQueryExecutorImpl<TestEntity> executor = (JpaCriteriaQueryExecutorImpl<TestEntity>) builder.buildExecutor();
        executor.setCached(true);
        TypedQuery<TestEntity> typedQuery = executor.getTypedQuery();
        Map<String, Object> hints = typedQuery.getHints();
        Assert.assertTrue(hints.containsKey("org.hibernate.cacheable"));
    }

    @Test
    public void testManyJoinFilter() {
        List<TestEntity> list = builder().buildExecutor().getResultList();
        for (int i = 0; i < list.size(); i++) {
            TestEntity test = list.get(i);
            for (int j = 0; j < i; j++) {
                RelatedEntity manyRelated = new RelatedEntity();
                manyRelated.setId(1000L + i * 100 + j);
                manyRelated.setStringValue("related" + j);
                manyRelated.setTestEntity(test);
                em.persist(manyRelated);
            }
        }

        String path = TestEntity.ATTR_manyRelatedValues + "." + RelatedEntity.ATTR_stringValue;
        assertEquals(4, builder().addFilter(path, FilterOperator.EQ, "related0").buildExecutor().getResultList().size());
        assertEquals(3, builder().addFilter(path, FilterOperator.EQ, "related1").buildExecutor().getResultList().size());
        assertEquals(2, builder().addFilter(path, FilterOperator.EQ, "related2").buildExecutor().getResultList().size());
        assertEquals(1, builder().addFilter(path, FilterOperator.EQ, "related3").buildExecutor().getResultList().size());
        assertEquals(0, builder().addFilter(path, FilterOperator.EQ, "related4").buildExecutor().getResultList().size());
    }
}
