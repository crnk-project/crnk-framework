package io.crnk.servlet.resource;

import io.crnk.client.CrnkClient;
import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.repository.RelationshipRepository;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.core.utils.Supplier;
import io.crnk.test.mock.models.RelationIdTestResource;
import io.crnk.test.mock.models.Schedule;
import io.crnk.test.mock.reactive.ReactiveTestModule;
import io.crnk.test.suite.TestContainer;

public class ReactiveServletTestContainer implements TestContainer {

	private final ReactiveTestModule testModule;

	private final CrnkBoot boot;

	private Supplier<CrnkClient> client;

	public ReactiveServletTestContainer(ReactiveTestModule testModule, Supplier<CrnkClient> client, CrnkBoot boot) {
		this.client = client;
		this.testModule = testModule;
		this.boot = boot;
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
	public <T, I, D, J> RelationshipRepository<T, I, D, J> getRepositoryForType(Class<T> sourceClass, Class<D> targetClass) {
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

	@Override
	public CrnkBoot getBoot() {
		return boot;
	}
}
