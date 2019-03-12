package io.crnk.monitor.opentracing.internal;

import java.net.MalformedURLException;
import java.net.URL;

import io.crnk.core.engine.dispatcher.Response;
import io.crnk.core.engine.filter.DocumentFilter;
import io.crnk.core.engine.filter.DocumentFilterChain;
import io.crnk.core.engine.filter.DocumentFilterContext;
import io.crnk.core.engine.http.HttpRequestContext;
import io.crnk.core.engine.http.HttpRequestContextProvider;
import io.crnk.core.engine.internal.dispatcher.path.JsonPath;
import io.opentracing.Span;
import io.opentracing.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenTracingFilter implements DocumentFilter {

	private static final Logger LOGGER = LoggerFactory.getLogger(OpenTracingFilter.class);

	private final HttpRequestContextProvider requestContextProvider;

	private final Tracer tracer;

	public OpenTracingFilter(HttpRequestContextProvider requestContextProvider, Tracer tracer) {
		this.requestContextProvider = requestContextProvider;
		this.tracer = tracer;
	}

	@Override
	public Response filter(DocumentFilterContext filterRequestContext, DocumentFilterChain chain) {
		JsonPath jsonPath = filterRequestContext.getJsonPath();
		Span span = tracer.activeSpan();

		if (span != null) {
			HttpRequestContext requestContext = requestContextProvider.getRequestContext();
			URL baseUrl;
			try {
				baseUrl = new URL(requestContext.getBaseUrl());
			}
			catch (MalformedURLException e) {
				throw new IllegalStateException("cannot parse base url", e);
			}
			String spanName = filterRequestContext.getMethod() + " " + baseUrl.getPath() + "/" + jsonPath.toGroupPath();

			LOGGER.debug("setting span name {} on {}", spanName, span);
			span.setOperationName(spanName);
		}
		else {
			LOGGER.debug("no span active");
		}

		return chain.doFilter(filterRequestContext);
	}
}
