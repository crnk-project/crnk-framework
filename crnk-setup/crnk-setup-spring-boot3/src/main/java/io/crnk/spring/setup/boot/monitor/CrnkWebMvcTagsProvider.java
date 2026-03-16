package io.crnk.spring.setup.boot.monitor;

import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.http.HttpRequestContext;
import io.crnk.core.engine.internal.dispatcher.path.JsonPath;
import io.crnk.core.engine.internal.dispatcher.path.PathBuilder;
import io.crnk.core.engine.internal.http.HttpRequestContextBaseAdapter;
import io.crnk.core.engine.internal.utils.UrlUtils;
import io.crnk.core.engine.parser.TypeParser;
import io.crnk.servlet.internal.ServletRequestContext;
import io.micrometer.common.KeyValue;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.observation.DefaultServerRequestObservationConvention;
import org.springframework.http.server.observation.ServerRequestObservationContext;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Boot 3 observation convention that preserves Crnk-specific low-cardinality `uri` values.
 */
public class CrnkWebMvcTagsProvider extends DefaultServerRequestObservationConvention {

	private static final Logger LOGGER = LoggerFactory.getLogger(CrnkWebMvcTagsProvider.class);

	private static final String URI_TAG = "uri";

	private final CrnkBoot boot;

	public CrnkWebMvcTagsProvider(CrnkBoot boot) {
		this.boot = boot;
		LOGGER.debug("initialized observation convention");
	}

	@Override
	protected KeyValue uri(ServerRequestObservationContext context) {
		HttpServletRequest request = context.getCarrier();
		if (request != null) {
			KeyValue uri = uri(request);
			if (uri != null) {
				return uri;
			}
		}
		return super.uri(context);
	}

	private KeyValue uri(HttpServletRequest request) {
		if (matchesPrefix(request)) {
			ServletContext servletContext = request.getServletContext();
			HttpRequestContext context = new HttpRequestContextBaseAdapter(
					new ServletRequestContext(servletContext, request, null, boot.getWebPathPrefix()));
			context.getQueryContext().initializeDefaults(boot.getResourceRegistry());

			String path = context.getPath();
			TypeParser typeParser = boot.getModuleRegistry().getTypeParser();
			PathBuilder pathBuilder = new PathBuilder(boot.getResourceRegistry(), typeParser);

			JsonPath jsonPath = pathBuilder.build(path, context.getQueryContext());
			if (jsonPath != null) {
				try {
					URL baseUrl = new URL(context.getBaseUrl());
					String uri = baseUrl.getPath() + "/" + jsonPath.toGroupPath();
					LOGGER.debug("computed observation uri={}", uri);
					return KeyValue.of(URI_TAG, uri);
				}
				catch (MalformedURLException e) {
					throw new IllegalStateException(e);
				}
			}
			LOGGER.debug("unknown path, using default observation uri for {}", request.getRequestURI());
		}
		return null;
	}

	private boolean matchesPrefix(HttpServletRequest request) {
		String pathPrefix = UrlUtils.removeLeadingSlash(boot.getWebPathPrefix());
		String path = UrlUtils.removeLeadingSlash(request.getRequestURI().substring(request.getContextPath().length()));
		return pathPrefix == null || path.startsWith(pathPrefix);
	}
}
