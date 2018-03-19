package io.crnk.example.wildfly;

import io.crnk.core.engine.transaction.TransactionRunner;
import io.crnk.example.wildfly.model.ScheduleEntity;
import io.crnk.home.HomeModule;
import io.crnk.jpa.JpaModule;
import io.crnk.jpa.JpaModuleConfig;
import io.crnk.jpa.JpaRepositoryConfig;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

public class WildflyModuleProducer {

	@PersistenceContext
	private EntityManager em;

	@Produces
	@ApplicationScoped
	public HomeModule produceHomeModule() {
		return HomeModule.create();
	}

	@Produces
	@ApplicationScoped
	public JpaModule produceJpaModule(TransactionRunner transactionRunner) {
		JpaModuleConfig config = new JpaModuleConfig();
		config.addRepository(JpaRepositoryConfig.create(ScheduleEntity.class));
		return JpaModule.createServerModule(config, em, transactionRunner);
	}
}
