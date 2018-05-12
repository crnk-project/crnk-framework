package io.crnk.servlet;

import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.dispatcher.RequestDispatcher;
import io.crnk.core.engine.http.HttpRequestContextProvider;
import io.crnk.core.engine.http.HttpResponse;
import io.crnk.core.engine.http.HttpStatus;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.engine.result.Result;
import io.crnk.core.engine.result.ResultFactory;
import io.crnk.core.engine.result.ImmediateResultFactory;
import io.crnk.servlet.internal.AsyncAdapter;
import io.crnk.servlet.internal.ServletModule;
import io.crnk.servlet.internal.ServletPropertiesProvider;
import io.crnk.servlet.internal.ServletRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

/**
 * Async/reactive servlet filter class to integrate with Crnk.
 * <p>
 * <p>
 * Child class can override {@link #initCrnk(CrnkBoot)} method and make use of CrnkBookt for further customizations.
 * </p>
 */
public class AsyncCrnkServlet extends HttpServlet {

	private static final Logger LOGGER = LoggerFactory.getLogger(AsyncCrnkServlet.class);

	protected CrnkBoot boot = new CrnkBoot();

	private Duration timeout = Duration.ofMillis(30000);

	public AsyncCrnkServlet() {
	}

	@Override
	public void init() {
		HttpRequestContextProvider provider = boot.getModuleRegistry().getHttpRequestContextProvider();
		boot.setPropertiesProvider(new ServletPropertiesProvider(getServletConfig()));
		boot.addModule(new ServletModule(provider));
		initCrnk(boot);
		boot.boot();
		if (!boot.getModuleRegistry().getResultFactory().isAsync()) {
			throw new IllegalStateException("make use of an async ResultFactory, e.g. provided by ReactiveModule");
		}
	}

	public CrnkBoot getBoot() {
		return boot;
	}

	public void setTimeout(Duration timeout) {
		this.timeout = timeout;
	}

	protected void initCrnk(CrnkBoot boot) {
		// nothing to do here
	}


	@Override
	public void service(ServletRequest req, ServletResponse res) throws IOException {
		PreconditionUtil
				.assertTrue("only http supported, ", req instanceof HttpServletRequest && res instanceof HttpServletResponse);

		HttpServletResponse httpResponse = (HttpServletResponse) res;

		ServletContext servletContext = getServletContext();
		ServletRequestContext context = new ServletRequestContext(servletContext, (HttpServletRequest) req,
				httpResponse, boot.getWebPathPrefix());
		RequestDispatcher requestDispatcher = boot.getRequestDispatcher();

		long startTime = System.currentTimeMillis();
		LOGGER.debug("setting up request");

		Optional<Result<HttpResponse>> optResponse = requestDispatcher.process(context);
		if (optResponse.isPresent()) {
			Result<HttpResponse> response = optResponse.get();
			response = response.setTimeout(timeout);

			AsyncContext asyncCtx = req.startAsync();

			// timeout fallback on http layer
			asyncCtx.setTimeout(timeout.toMillis() + 2000);

			asyncCtx.addListener(new AsyncAdapter() {

				@Override
				public void onTimeout(AsyncEvent event) throws IOException {
					LOGGER.error("timeout for request {}", event);
					httpResponse.setStatus(HttpStatus.GATEWAY_TIMEOUT_504);

				}

			});
			response.subscribe(it -> {
						LOGGER.debug("writing response");
						it.getHeaders().entrySet().forEach(entry -> httpResponse.setHeader(entry.getKey(), entry.getValue()));
						httpResponse.setStatus(it.getStatusCode());
						try (ServletOutputStream outputStream = httpResponse.getOutputStream()) {
							byte[] body = it.getBody();
							if (body != null) {
								LOGGER.debug("response bodyLength={}", body.length);
								outputStream.write(body);
							}
						} catch (Exception e) {
							LOGGER.error("failed to process request", e);
						} finally {
							asyncCtx.complete();
							LOGGER.debug("response completed");
						}
					}, exception -> {
						LOGGER.error("failed to process request", exception);
						if (exception instanceof TimeoutException) {
							httpResponse.setStatus(HttpStatus.GATEWAY_TIMEOUT_504);
						} else {
							httpResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500);
						}
						asyncCtx.complete();
					}
			);
		} else {
			httpResponse.setStatus(HttpStatus.NOT_FOUND_404);
		}

		long endTime = System.currentTimeMillis();
		LOGGER.debug("prepared request in in {}ms", endTime - startTime);
	}
}
