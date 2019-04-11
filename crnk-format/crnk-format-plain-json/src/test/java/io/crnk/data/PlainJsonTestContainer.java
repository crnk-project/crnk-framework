package io.crnk.data;

import io.crnk.client.CrnkClient;
import io.crnk.client.http.inmemory.InMemoryHttpAdapter;
import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.repository.RelationshipRepository;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.format.plainjson.PlainJsonFormatModule;
import io.crnk.test.mock.TestModule;
import io.crnk.test.mock.models.RelationIdTestResource;
import io.crnk.test.mock.models.Schedule;
import io.crnk.test.mock.repository.RelationIdTestRepository;
import io.crnk.test.mock.repository.ScheduleRepositoryImpl;
import io.crnk.test.suite.TestContainer;

import java.io.Serializable;

public class PlainJsonTestContainer implements TestContainer {

    private CrnkBoot boot;

    private CrnkClient client;

    @Override
    public void start() {
        TestModule.clear();

        boot = new CrnkBoot();
        boot.addModule(new TestModule());
        boot.addModule(new PlainJsonFormatModule());
        boot.boot();

        client = new CrnkClient("http://127.0.0.1:8080");
        client.setHttpAdapter(new InMemoryHttpAdapter(boot, "http://127.0.0.1:8080"));
        client.addModule(new PlainJsonFormatModule());
    }

    @Override
    public void stop() {
    }

    @Override
    public <T, I extends Serializable> ResourceRepository<T, I> getRepositoryForType(Class<T> resourceClass) {
        return client.getRepositoryForType(resourceClass);
    }

    @Override
    public <T, I extends Serializable, D, J extends Serializable> RelationshipRepository<T, I, D, J> getRepositoryForType(Class<T> sourceClass, Class<D> targetClass) {
        return client.getRepositoryForType(sourceClass, targetClass);
    }

    @Override
    public <T> T getTestData(Class<T> clazz, Object id) {
        if (clazz == Schedule.class) {
            return (T) ScheduleRepositoryImpl.schedules.get(id);
        }
        if (clazz == RelationIdTestResource.class) {
            return (T) RelationIdTestRepository.resources.get(id);
        }
        throw new UnsupportedOperationException();
    }

    public CrnkClient getClient() {
        return client;
    }

    @Override
    public String getBaseUrl() {
        return client.getServiceUrlProvider().getUrl();
    }

    @Override
    public CrnkBoot getBoot() {
        return boot;
    }
}
