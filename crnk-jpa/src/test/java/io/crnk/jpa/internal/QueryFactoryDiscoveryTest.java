package io.crnk.jpa.internal;

import io.crnk.core.engine.transaction.TransactionRunner;
import io.crnk.jpa.JpaModule;
import io.crnk.jpa.query.criteria.JpaCriteriaQueryFactory;
import io.crnk.jpa.query.querydsl.QuerydslQueryFactory;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import javax.persistence.EntityManager;

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
		TransactionRunner transactionRunner = Mockito.mock(TransactionRunner.class);
		EntityManager em = Mockito.mock(EntityManager.class);
		JpaModule module = JpaModule.newServerModule(em, transactionRunner);
		Assert.assertEquals(QuerydslQueryFactory.class, module.getQueryFactory().getClass());
	}
}
