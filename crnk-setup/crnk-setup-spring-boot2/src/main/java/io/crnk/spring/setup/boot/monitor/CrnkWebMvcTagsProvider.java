package io.crnk.spring.setup.boot.monitor;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.http.HttpRequestContext;
import io.crnk.core.engine.internal.dispatcher.path.JsonPath;
import io.crnk.core.engine.internal.dispatcher.path.PathBuilder;
import io.crnk.core.engine.internal.http.HttpRequestContextBaseAdapter;
import io.crnk.core.engine.internal.utils.UrlUtils;
import io.crnk.core.engine.parser.TypeParser;
import io.crnk.servlet.internal.ServletRequestContext;
import io.micrometer.core.instrument.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.metrics.web.servlet.DefaultWebMvcTagsProvider;
import org.springframework.boot.actuate.metrics.web.servlet.WebMvcTagsProvider;

/**
 * Crnk {@link WebMvcTagsProvider} implementation extends built-in {@link DefaultWebMvcTagsProvider} and
 * overrides uri tag recognition logic for resources in order to have proper `uri` value. Uses base class values as fallback.
 */
public class CrnkWebMvcTagsProvider extends DefaultWebMvcTagsProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(CrnkWebMvcTagsProvider.class);

	private static final String URI_TAG = "uri";

	private final CrnkBoot boot;

	public CrnkWebMvcTagsProvider(CrnkBoot boot) {
		this.boot = boot;
		LOGGER.debug("initialized tag provider");
	}

	@Override
	public Iterable<Tag> getTags(HttpServletRequest request, HttpServletResponse response, Object handler, Throwable exception) {
		LOGGER.debug("getTags for {}", request.getRequestURI());
		Iterable<Tag> tags = super.getTags(request, response, handler, exception);
		return enhanceUri(tags, request);
	}

	@Override
	public Iterable<Tag> getLongRequestTags(HttpServletRequest request, Object handler) {
		LOGGER.debug("getLongRequestTags for {}", request.getRequestURI());
		Iterable<Tag> tags = super.getLongRequestTags(request, handler);
		return enhanceUri(tags, request);
	}

	private Iterable<Tag> enhanceUri(Iterable<Tag> tags, HttpServletRequest request) {
		Tag uri = uri(request);
		if (uri != null) {
			List<Tag> enhancedTags = new ArrayList<>();
			for (Tag tag : tags) {
				if (tag.getKey().equals(URI_TAG)) {
					enhancedTags.add(uri);
				}
				else {
					enhancedTags.add(tag);
				}
			}
			return enhancedTags;
		}
		return tags;
	}

	private Tag uri(final HttpServletRequest request) {
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
				return Tag.of(URI_TAG, uri);
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
