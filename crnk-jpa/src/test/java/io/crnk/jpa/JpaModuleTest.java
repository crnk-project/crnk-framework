package io.crnk.jpa;

import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.transaction.TransactionRunner;
import io.crnk.jpa.model.TestEntity;
import org.junit.Assert;
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

	@Test
	public void checkInvalidConfig() {
		JpaModuleConfig config = new JpaModuleConfig();
		config.addRepository(JpaRepositoryConfig.create(String.class));

		CrnkBoot boot = new CrnkBoot();
		EntityManager em = Mockito.mock(EntityManager.class);
		TransactionRunner transactionManager = Mockito.mock(TransactionRunner.class);
		boot.addModule(JpaModule.createServerModule(config, em, transactionManager));
		try {
			boot.boot();
		} catch (IllegalStateException e) {
			// ok
			Assert.assertEquals(e.getMessage(),
					"failed to gather entity informations from class java.lang.String, make sure it is probably annotated with "
							+ "JPA annotations");
		}
	}
}
