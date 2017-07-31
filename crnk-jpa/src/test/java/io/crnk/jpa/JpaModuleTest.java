package io.crnk.jpa;

import io.crnk.core.engine.transaction.TransactionRunner;
import io.crnk.jpa.model.TestEntity;
import org.junit.Test;
import org.mockito.Mockito;

import javax.persistence.EntityManager;

public class JpaModuleTest {


	@Test(expected = IllegalStateException.class)
	public void cannotPerformDuplicateRegistration() {
		TransactionRunner transactionRunner = Mockito.mock(TransactionRunner.class);
		EntityManager em = Mockito.mock(EntityManager.class);
		JpaModule module = JpaModule.newServerModule(em, transactionRunner);
		module.addRepository(JpaRepositoryConfig.create(TestEntity.class));
		module.addRepository(JpaRepositoryConfig.create(TestEntity.class));
	}

}
