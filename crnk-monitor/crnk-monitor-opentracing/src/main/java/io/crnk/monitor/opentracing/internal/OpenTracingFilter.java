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

	private final boolean simpleTransactionNames;

	public OpenTracingFilter(HttpRequestContextProvider requestContextProvider, Tracer tracer, boolean simpleTransactionNames) {
		this.requestContextProvider = requestContextProvider;
		this.tracer = tracer;
		this.simpleTransactionNames = simpleTransactionNames;
	}

	@Override
	public Response filter(DocumentFilterContext filterRequestContext, DocumentFilterChain chain) {
		Span span = tracer.activeSpan();

		if (span != null) {
			String spanName = toSpanName(filterRequestContext);

			LOGGER.debug("setting span name {} on {}", spanName, span);
			span.setOperationName(spanName);
		}
		else {
			LOGGER.debug("no span active");
		}

		return chain.doFilter(filterRequestContext);
	}

	private String toSpanName(DocumentFilterContext filterRequestContext) {
		HttpRequestContext requestContext = requestContextProvider.getRequestContext();

		JsonPath jsonPath = filterRequestContext.getJsonPath();
		URL baseUrl;
		try {
			baseUrl = new URL(requestContext.getBaseUrl());
		}
		catch (MalformedURLException e) {
			throw new IllegalStateException("cannot parse base url", e);
		}
		String name = filterRequestContext.getMethod() + " " + baseUrl.getPath() + "/" + jsonPath.toGroupPath();
		if (simpleTransactionNames) {
			return toSimpleName(name);
		}
		return name;
	}

	private String toSimpleName(String name) {
		StringBuilder builder = new StringBuilder();
		int i = 0;
		while (i < name.length()) {
			char c = name.charAt(i);
			if (c == ' ') {
				builder.append('_');
			}
			else if (c == '/' && i < name.length() - 1 && (i == 0 || name.charAt(i - 1) != ' ')) {
				builder.append(Character.toUpperCase(name.charAt(i + 1)));
				i++;
			}
			else if (c != '{' && c != '}' && c != '/') {
				builder.append(c);
			}
			i++;
		}
		return builder.toString();
	}
}
