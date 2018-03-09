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
import java.util.Optional;

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

	private int timeout = 30000;

	public AsyncCrnkServlet() {
	}

	public void setResultFactory(ResultFactory resultFactory) {
		boot.getModuleRegistry().setResultFactory(resultFactory);
	}

	@Override
	public void init() {
		if (boot.getModuleRegistry().getResultFactory() instanceof ImmediateResultFactory) {
			throw new IllegalStateException("call setResultFactory with a async implementation");
		}
		HttpRequestContextProvider provider = boot.getModuleRegistry().getHttpRequestContextProvider();
		boot.setPropertiesProvider(new ServletPropertiesProvider(getServletConfig()));
		boot.addModule(new ServletModule(provider));
		initCrnk(boot);
		boot.boot();
	}

	@Override
	public void destroy() {
	}


	public CrnkBoot getBoot() {
		return boot;
	}

	public void setTimeout(int timeout) {
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
			AsyncContext asyncCtx = req.startAsync();
			asyncCtx.addListener(new CrnkAsyncListener());
			asyncCtx.setTimeout(timeout);
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
						} catch (IOException e) {
							LOGGER.error("failed to process request", e);
						} finally {
							asyncCtx.complete();
							LOGGER.debug("response completed");
						}
					}, exception -> LOGGER.error("failed to process request", exception)
			);
		} else {
			httpResponse.setStatus(HttpStatus.NOT_FOUND_404);
		}

		long endTime = System.currentTimeMillis();
		LOGGER.debug("prepared request in in {}ms", endTime - startTime);
	}

	public class CrnkAsyncListener implements AsyncListener {
		public void onComplete(AsyncEvent event) throws IOException {
			log("onComplete called");
		}

		public void onTimeout(AsyncEvent event) throws IOException {
			log("onTimeout called");
		}

		public void onError(AsyncEvent event) throws IOException {
			log("onError called");
		}

		public void onStartAsync(AsyncEvent event) throws IOException {
			log("onStartAsync called");
		}
	}
}
