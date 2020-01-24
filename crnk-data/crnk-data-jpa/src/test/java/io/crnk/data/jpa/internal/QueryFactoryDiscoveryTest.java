package io.crnk.data.jpa.internal;

import io.crnk.data.jpa.JpaModuleConfig;
import io.crnk.data.jpa.query.criteria.JpaCriteriaQueryFactory;
import io.crnk.data.jpa.query.querydsl.QuerydslQueryFactory;
import org.junit.Assert;
import org.junit.Test;

public class QueryFactoryDiscoveryTest {

    @Test
    public void checkDiscoverQueryDsl() {
        QueryFactoryDiscovery discovery = new QueryFactoryDiscovery();
        Assert.assertEquals(QuerydslQueryFactory.class, discovery.discoverDefaultFactory().getClass());
    }

    @Test
    public void checkDiscoverCriteriaApi() {
        QueryFactoryDiscovery discovery = new QueryFactoryDiscovery();

        ClassLoader bootstrapClassLoader = ClassLoader.getSystemClassLoader().getParent();
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(bootstrapClassLoader);
        try {
            Assert.assertEquals(JpaCriteriaQueryFactory.class, discovery.discoverDefaultFactory().getClass());
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
    }

    @Test
    public void checkJpaModuleIntegration() {
        JpaModuleConfig config = new JpaModuleConfig();
        Assert.assertEquals(QuerydslQueryFactory.class, config.getQueryFactory().getClass());
    }
}
