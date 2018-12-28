package io.crnk.spring.cloud.sleuth.internal;

import io.crnk.core.engine.dispatcher.RepositoryRequestSpec;
import io.crnk.core.engine.filter.RepositoryFilterBase;
import io.crnk.core.engine.filter.RepositoryFilterContext;
import io.crnk.core.engine.filter.RepositoryRequestFilterChain;
import io.crnk.core.module.Module.ModuleContext;
import io.crnk.core.repository.response.JsonApiResponse;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;

import java.util.Collection;

/**
 * Performs a local trace for each repository call. Keep in mind that a single HTTP request
 * can trigger multiple repository calls if inclusions of relations are in use. .
 */
public class SleuthRepositoryFilter extends RepositoryFilterBase {

	public static final String STRING_EXCEPTION = "EXCEPTION";

	public static final String STRING_OK = "OK";

	public static final String QUERY_RESULTS = "crnk.results";

	public static final String STATUS_CODE_ANNOTATION = "crnk.status";

	private static final String QUERY_ANNOTATION = "crnk.query";

	protected static final String COMPONENT_NAME = "crnk";

	protected static final Object COMPONENT_NAME_SEPARATOR = ":";

	private Tracer tracer;

	private ModuleContext moduleContext;

	public SleuthRepositoryFilter(Tracer tracer, ModuleContext context) {
		this.tracer = tracer;
		this.moduleContext = context;
	}

	@Override
	public JsonApiResponse filterRequest(RepositoryFilterContext context, RepositoryRequestFilterChain chain) {
		RepositoryRequestSpec request = context.getRequest();
		String query = SleuthUtil.getQuery(request, moduleContext.getModuleRegistry().getUrlMapper());

		Span span = tracer.createSpan(SleuthUtil.getSpanName(request));

		JsonApiResponse result = null;
		Exception exception = null;
		try {
			span.tag("lc", COMPONENT_NAME);

			result = chain.doFilter(context);
			return result;
		} catch (RuntimeException e) {
			exception = e;
			throw e;
		} finally {
			boolean resultError = result != null && result.getErrors() != null && result.getErrors().iterator().hasNext();
			boolean failed = exception != null || resultError;
			String status = failed ? STRING_EXCEPTION : STRING_OK;

			span.tag(STATUS_CODE_ANNOTATION, status);
			writeQuery(span, query);
			writeResults(span, result);
			tracer.close(span);
		}
	}

	private void writeQuery(Span span, String query) {
		if (query != null) {
			span.tag(QUERY_ANNOTATION, query);
		}
	}

	private void writeResults(Span span, JsonApiResponse result) {
		if (result != null && result.getEntity() != null) {
			int numResults = getResultCount(result);
			span.tag(QUERY_RESULTS, Integer.toString(numResults));
		}
	}

	private int getResultCount(JsonApiResponse result) {
		return result.getEntity() instanceof Collection ? ((Collection<?>) result.getEntity()).size() : 1;
	}
}
