package io.crnk.spring.setup.boot.metrics;

import io.crnk.core.engine.registry.ResourceRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import org.apache.logging.log4j.util.Strings;
import org.springframework.boot.actuate.metrics.web.servlet.DefaultWebMvcTagsProvider;
import org.springframework.boot.actuate.metrics.web.servlet.WebMvcTags;
import org.springframework.boot.actuate.metrics.web.servlet.WebMvcTagsProvider;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.regex.Pattern;

/**
 * Crnk {@link WebMvcTagsProvider} implementation extends built-in {@link DefaultWebMvcTagsProvider} and
 * overrides uri tag recognition logic for resources in order to have proper `uri` value. Uses base class values as fallback.
 */
public class CrnkWebMvcTagsProvider extends DefaultWebMvcTagsProvider {

	private static final String SEPARATOR = "/";

	private static final Pattern SEPARATOR_PATTER = Pattern.compile(SEPARATOR);

	private final ResourceRegistry resourceRegistry;

	public CrnkWebMvcTagsProvider(final ResourceRegistry resourceRegistry) {
		this.resourceRegistry = resourceRegistry;
	}

	@Override
	public Iterable<Tag> getTags(HttpServletRequest request, HttpServletResponse response, Object handler, Throwable exception) {
		Tag uri = uri(request);
		if (uri != null) {
			return Tags.of(WebMvcTags.method(request), uri(request), WebMvcTags.exception(exception), WebMvcTags.status(response));
		}

		return super.getTags(request, response, handler, exception);
	}

	@Override
	public Iterable<Tag> getLongRequestTags(HttpServletRequest request, Object handler) {
		Tag uri = uri(request);
		if (uri != null) {
			return Tags.of(WebMvcTags.method(request), uri);
		}

		return super.getLongRequestTags(request, handler);
	}

	private Tag uri(final HttpServletRequest request) {
		String uri = request.getRequestURI();
		if (uri.startsWith(SEPARATOR)) {
			uri = uri.substring(1);
		}
		String[] segments = SEPARATOR_PATTER.split(uri);
		if (segments.length == 0 || resourceRegistry.getEntryByPath(segments[0]) == null) {
			return null;
		}
		if (segments.length > 1) {
			segments[1] = "{id}";
		}

		return Tag.of("uri", SEPARATOR + String.join(SEPARATOR, segments));
	}
}
