package io.crnk.test.suite;

import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.repository.RelationshipRepository;
import io.crnk.core.repository.ResourceRepository;

import java.io.Serializable;

public interface TestContainer {

    void start();

    void stop();

    <T, I extends Serializable> ResourceRepository<T, I> getRepositoryForType(Class<T> resourceClass);

    <T, I extends Serializable, D, J extends Serializable> RelationshipRepository<T, I, D, J> getRepositoryForType(
            Class<T> sourceClass, Class<D> targetClass);

    <T> T getTestData(Class<T> clazz, Object id);

    String getBaseUrl();

    CrnkBoot getBoot();
}
