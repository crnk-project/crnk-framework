package io.crnk.client.suite;

import io.crnk.client.AbstractClientTest;
import io.crnk.client.CrnkClient;
import io.crnk.core.repository.RelationshipRepositoryV2;
import io.crnk.core.repository.ResourceRepositoryV2;
import io.crnk.test.mock.models.RelationIdTestResource;
import io.crnk.test.mock.models.Schedule;
import io.crnk.test.mock.repository.RelationIdTestRepository;
import io.crnk.test.mock.repository.ScheduleRepositoryImpl;
import io.crnk.test.suite.TestContainer;

import java.io.Serializable;
import java.util.function.Consumer;

public class ClientTestContainer implements TestContainer {

	public static void prepare() {
		AbstractClientTest.selectPort();
	}

	private AbstractClientTest test;

	private Consumer<CrnkClient> clientSetupConsumer;

	@Override
	public void start() {
		test = new AbstractClientTest() {

			@Override
			protected TestApplication configure() {
				return new TestApplication();
			}

			@Override
			protected void setupClient(CrnkClient client) {
				if (clientSetupConsumer != null) {
					clientSetupConsumer.accept(client);
				}
			}
		};
		try {
			test.setUp();
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
		test.setup();
	}

	public void setClientSetupConsumer(Consumer<CrnkClient> clientSetupConsumer) {
		this.clientSetupConsumer = clientSetupConsumer;
	}

	@Override
	public void stop() {
		try {
			test.tearDown();
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public <T, I extends Serializable> ResourceRepositoryV2<T, I> getRepositoryForType(Class<T> resourceClass) {
		return test.client.getRepositoryForType(resourceClass);
	}

	@Override
	public <T, I extends Serializable, D, J extends Serializable> RelationshipRepositoryV2<T, I, D, J> getRepositoryForType(Class<T> sourceClass, Class<D> targetClass) {
		return test.client.getRepositoryForType(sourceClass, targetClass);
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
		return test.client;
	}

	@Override
	public String getBaseUrl() {
		return test.client.getServiceUrlProvider().getUrl();
	}
}
