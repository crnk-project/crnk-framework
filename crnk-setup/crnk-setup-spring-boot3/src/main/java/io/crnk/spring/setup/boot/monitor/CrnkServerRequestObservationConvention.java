package io.crnk.spring.setup.boot.monitor;

import groovy.util.logging.Slf4j;
import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.http.HttpRequestContext;
import io.crnk.core.engine.internal.dispatcher.path.JsonPath;
import io.crnk.core.engine.internal.dispatcher.path.PathBuilder;
import io.crnk.core.engine.internal.http.HttpRequestContextBaseAdapter;
import io.crnk.core.engine.internal.utils.UrlUtils;
import io.crnk.core.engine.parser.TypeParser;
import io.crnk.servlet.internal.ServletRequestContext;
import io.micrometer.common.KeyValue;
import io.micrometer.common.KeyValues;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.observation.DefaultServerRequestObservationConvention;
import org.springframework.http.server.observation.ServerRequestObservationContext;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class CrnkServerRequestObservationConvention extends DefaultServerRequestObservationConvention {
	private static final Logger LOGGER = LoggerFactory.getLogger(CrnkServerRequestObservationConvention.class);

	private static final String URI_TAG = "uri";
	private final CrnkBoot boot;

	public CrnkServerRequestObservationConvention(CrnkBoot boot) {
		this.boot = boot;
		LOGGER.debug("initialized observation convention");
	}

	@NotNull
	@Override
	public KeyValues getLowCardinalityKeyValues(ServerRequestObservationContext context) {
		HttpServletRequest request = context.getCarrier();
		LOGGER.debug("getLowCardinalityKeyValues for {}", request.getRequestURI());
		KeyValues keyValues = super.getLowCardinalityKeyValues(context);
		return enhanceUri(keyValues, request);
	}

	@NotNull
	@Override
	public KeyValues getHighCardinalityKeyValues(ServerRequestObservationContext context) {
		HttpServletRequest request = context.getCarrier();
		LOGGER.debug("getHighCardinalityKeyValues for {}", request.getRequestURI());
		KeyValues keyValues = super.getHighCardinalityKeyValues(context);
		return enhanceUri(keyValues, request);
	}

	private KeyValues enhanceUri(Iterable<KeyValue> keyValues, HttpServletRequest request) {
		KeyValue uri = uri(request);
		if (uri != null) {
			List<KeyValue> enhancedKeyValues = new ArrayList<>();
			for (KeyValue keyValue : keyValues) {
				if (keyValue.getKey().equals(URI_TAG)) {
					enhancedKeyValues.add(uri);
				}
				else {
					enhancedKeyValues.add(keyValue);
				}
			}
			return KeyValues.of(enhancedKeyValues);
		}
		return KeyValues.of(keyValues);
	}

	private KeyValue uri(final HttpServletRequest request) {
		if (matchesPrefix(request)) {
			ServletContext servletContext = request.getServletContext();
			HttpRequestContext context = new HttpRequestContextBaseAdapter(new ServletRequestContext(servletContext, request, null, boot.getWebPathPrefix()));
			context.getQueryContext().initializeDefaults(boot.getResourceRegistry());

			String path = context.getPath();


			TypeParser typeParser = boot.getModuleRegistry().getTypeParser();
			PathBuilder pathBuilder = new PathBuilder(boot.getResourceRegistry(), typeParser);

			JsonPath jsonPath = pathBuilder.build(path, context.getQueryContext());
			if (jsonPath != null) {
				URL baseUrl;
				try {
					baseUrl = new URL(context.getBaseUrl());
				}
				catch (MalformedURLException e) {
					throw new IllegalStateException(e);
				}
				String uri = baseUrl.getPath() + "/" + jsonPath.toGroupPath();
				LOGGER.debug("computed mvc tag: uri={}", uri);
				return KeyValue.of(URI_TAG, uri);
			}
			LOGGER.debug("unknown path, using default mvc tags: uri={}", request.getRequestURI());
		}
		return null;
	}

	private boolean matchesPrefix(HttpServletRequest request) {
		String pathPrefix = UrlUtils.removeLeadingSlash(boot.getWebPathPrefix());
		String path = UrlUtils.removeLeadingSlash(request.getRequestURI().substring(request.getContextPath().length()));
		return pathPrefix == null || path.startsWith(pathPrefix);
	}
}
