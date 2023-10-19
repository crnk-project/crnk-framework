package io.crnk.example.openliberty.microprofile;

import io.crnk.core.engine.transaction.TransactionRunner;
import io.crnk.data.jpa.JpaModule;
import io.crnk.data.jpa.JpaModuleConfig;
import io.crnk.home.HomeModule;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class BeanProducer {

	@Produces
	public EntityManager produceEm() {
		EntityManagerFactory emfactory = Persistence.createEntityManagerFactory( "TEST_LIBERTY" );
		return emfactory.createEntityManager( );
	}

	@Produces
	@ApplicationScoped
	public HomeModule produceHomeModule() {
		return HomeModule.create();
	}

	@Produces
	@ApplicationScoped
	public JpaModule produceJpaModule(TransactionRunner transactionRunner, EntityManager em) {
		JpaModuleConfig config = new JpaModuleConfig();
		return JpaModule.createServerModule(config, em, transactionRunner);
	}
}
