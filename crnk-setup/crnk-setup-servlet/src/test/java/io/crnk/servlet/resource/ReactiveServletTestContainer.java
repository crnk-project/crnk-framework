package io.crnk.servlet.resource;

import io.crnk.client.CrnkClient;
import io.crnk.core.repository.RelationshipRepositoryV2;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.core.utils.Supplier;
import io.crnk.test.mock.models.RelationIdTestResource;
import io.crnk.test.mock.models.Schedule;
import io.crnk.test.mock.reactive.ReactiveTestModule;
import io.crnk.test.suite.TestContainer;

import java.io.Serializable;

public class ReactiveServletTestContainer implements TestContainer {

	private final ReactiveTestModule testModule;
	private Supplier<CrnkClient> client;

	public ReactiveServletTestContainer(ReactiveTestModule testModule, Supplier<CrnkClient> client) {
		this.client = client;
		this.testModule = testModule;
	}

	@Override
	public void start() {
		testModule.clear();
	}

	@Override
	public void stop() {
		testModule.clear();
	}

	@Override
	public <T, I> ResourceRepository<T, I> getRepositoryForType(Class<T> resourceClass) {
		return client.get().getRepositoryForType(resourceClass);
	}

	@Override
	public <T, I extends Serializable, D, J extends Serializable> RelationshipRepositoryV2<T, I, D, J> getRepositoryForType(Class<T> sourceClass, Class<D> targetClass) {
		return client.get().getRepositoryForType(sourceClass, targetClass);
	}

	@Override
	public <T> T getTestData(Class<T> clazz, Object id) {
		if (clazz == Schedule.class) {
			return (T) testModule.getScheduleRepository().getMap().get(id);
		}
		if (clazz == RelationIdTestResource.class) {
			return (T) testModule.getRelationIdTestRepository().getMap().get(id);
		}
		throw new UnsupportedOperationException();
	}

	@Override
	public String getBaseUrl() {
		return client.get().getServiceUrlProvider().getUrl();
	}
}
