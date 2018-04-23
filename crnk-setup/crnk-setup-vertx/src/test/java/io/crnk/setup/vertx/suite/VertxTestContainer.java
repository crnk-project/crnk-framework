package io.crnk.setup.vertx.suite;

import io.crnk.client.CrnkClient;
import io.crnk.core.repository.RelationshipRepositoryV2;
import io.crnk.core.repository.ResourceRepositoryV2;
import io.crnk.core.utils.Supplier;
import io.crnk.setup.vertx.CrnkVerticle;
import io.crnk.test.mock.ClientTestModule;
import io.crnk.test.mock.models.RelationIdTestResource;
import io.crnk.test.mock.models.Schedule;
import io.crnk.test.suite.TestContainer;
import io.reactivex.subjects.SingleSubject;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;

import java.io.IOException;
import java.io.Serializable;
import java.net.ServerSocket;

public class VertxTestContainer implements TestContainer {

	private CrnkVerticle vehicle;

	private Supplier<CrnkClient> client;

	private Vertx vertx;

	private int port;

	public VertxTestContainer() {
		client = () -> {
			CrnkClient client = new CrnkClient(this.getBaseUrl());
			client.addModule(new ClientTestModule());
			return client;
		};
	}

	@Override
	public void start() {
		try {
			ServerSocket socket = new ServerSocket(0);
			port = socket.getLocalPort();
			socket.close();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}

		VertxOptions options = new VertxOptions();
		options.setMaxEventLoopExecuteTime(Long.MAX_VALUE);

		SingleSubject waitSubject = SingleSubject.create();
		Handler<AsyncResult<String>> completionHandler = event -> waitSubject.onSuccess(event.result());

		vehicle = new CrnkVerticle(port);
		vertx = Vertx.vertx(options);
		vertx.deployVerticle(vehicle, completionHandler);
		vehicle.testModule.clear();
		waitSubject.blockingGet();
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public void stop() {
		SingleSubject waitSubject = SingleSubject.create();
		Handler<AsyncResult<Void>> completionHandler = event -> waitSubject.onSuccess("test");
		vertx.close(completionHandler);
		waitSubject.blockingGet();

		vehicle.testModule.clear();
		vertx = null;
		vehicle = null;
		port = -1;
	}

	@Override
	public <T, I extends Serializable> ResourceRepositoryV2<T, I> getRepositoryForType(Class<T> resourceClass) {
		return client.get().getRepositoryForType(resourceClass);
	}

	@Override
	public <T, I extends Serializable, D, J extends Serializable> RelationshipRepositoryV2<T, I, D, J> getRepositoryForType(Class<T> sourceClass, Class<D> targetClass) {
		return client.get().getRepositoryForType(sourceClass, targetClass);
	}

	@Override
	public <T> T getTestData(Class<T> clazz, Object id) {
		if (clazz == Schedule.class) {
			return (T) vehicle.testModule.getScheduleRepository().getMap().get((Long) id);
		}
		if (clazz == RelationIdTestResource.class) {
			return (T) vehicle.testModule.getRelationIdTestRepository().getMap().get((Long) id);
		}
		throw new UnsupportedOperationException();
	}

	@Override
	public String getBaseUrl() {
		return "http://127.0.0.1:" + port;
	}
}
