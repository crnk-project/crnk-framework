package io.crnk.setup.vertx;

import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.dispatcher.RequestDispatcher;
import io.crnk.core.engine.http.HttpResponse;
import io.crnk.core.engine.http.HttpStatus;
import io.crnk.core.engine.result.Result;
import io.crnk.reactive.ReactiveModule;
import io.crnk.reactive.internal.MonoResult;
import io.crnk.setup.vertx.internal.VertxModule;
import io.crnk.setup.vertx.internal.VertxRequestContext;
import io.reactivex.subjects.SingleSubject;
import io.vertx.core.Handler;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.core.http.HttpServerRequest;
import io.vertx.reactivex.core.http.HttpServerResponse;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Consumer;


public class CrnkVertxHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(CrnkVertxHandler.class);

	protected CrnkBoot boot = new CrnkBoot();

	public CrnkVertxHandler() {
		this((boot) -> {
		});
	}

	public CrnkVertxHandler(Consumer<CrnkBoot> configurer) {
		configurer.accept(boot);
		boot.addModule(new VertxModule());
		boot.addModule(new ReactiveModule());
		boot.boot();
		if (!boot.getModuleRegistry().getResultFactory().isAsync()) {
			throw new IllegalStateException("make use of an async ResultFactory, e.g. provided by ReactiveModule");
		}
	}

	public CrnkBoot getBoot() {
		return boot;
	}

	public Publisher<HttpServerRequest> process(HttpServerRequest serverRequest) {
		VertxRequestContext context = new VertxRequestContext(serverRequest, boot.getWebPathPrefix());
		Mono<VertxRequestContext> mono = Mono.just(context);
		SingleSubject<VertxRequestContext> bodySubject = SingleSubject.create();
		Handler<Buffer> bodyHandler = (event) -> {
			// TODO encoding, string vs byte[]
			context.setRequestBody(event.toString().getBytes());
			bodySubject.onSuccess(context);
		};
		serverRequest.bodyHandler(bodyHandler);

		Mono waitForBody = Mono.from(bodySubject.toFlowable());
		return mono.flatMap(it -> waitForBody).flatMap(it -> processRequest(context));

	}

	private Mono<HttpServerRequest> processRequest(VertxRequestContext context) {
		HttpServerRequest serverRequest = context.getServerRequest();
		RequestDispatcher requestDispatcher = boot.getRequestDispatcher();

		long startTime = System.currentTimeMillis();
		LOGGER.debug("setting up request");

		try {
			Optional<Result<HttpResponse>> optResponse = requestDispatcher.process(context);

			if (optResponse.isPresent()) {
				MonoResult<HttpResponse> response = (MonoResult<HttpResponse>) optResponse.get();

				Mono<HttpResponse> mono = response.getMono();
				return mono.map(it -> {
					HttpServerResponse httpResponse = serverRequest.response();
					LOGGER.debug("delivering response {}", httpResponse);
					httpResponse.setStatusCode(it.getStatusCode());
					it.getHeaders().forEach((key, value) -> httpResponse.putHeader(key, value));
					if (it.getBody() != null) {
						Buffer bodyBuffer = Buffer.newInstance(io.vertx.core.buffer.Buffer.buffer(it.getBody()));
						httpResponse.end(bodyBuffer);
					} else {
						httpResponse.end();
					}
					return serverRequest;
				});
			} else {
				serverRequest.response().setStatusCode(HttpStatus.NOT_FOUND_404);
				serverRequest.response().end();
				return Mono.just(serverRequest);
			}

		} catch (IOException e) {
			throw new IllegalStateException(e);
		} finally {
			long endTime = System.currentTimeMillis();
			LOGGER.debug("prepared request in in {}ms", endTime - startTime);
		}
	}
}
