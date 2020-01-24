package io.crnk.example.wildfly;

import io.crnk.core.engine.transaction.TransactionRunner;
import io.crnk.example.wildfly.endpoints.ScheduleRepository;
import io.crnk.example.wildfly.endpoints.UserRepository;
import io.crnk.home.HomeModule;
import io.crnk.data.jpa.JpaModule;
import io.crnk.data.jpa.JpaModuleConfig;

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
        return JpaModule.createServerModule(config, em, transactionRunner);
    }

    @Produces
    @ApplicationScoped
    public ScheduleRepository scheduleRepository() {
        return new ScheduleRepository();
    }

    @Produces
    @ApplicationScoped
    public UserRepository userRepository() {
        return new UserRepository();
    }
}
