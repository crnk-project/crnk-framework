package io.crnk.brave.internal;

import java.util.Collection;

import com.github.kristofa.brave.Brave;
import com.github.kristofa.brave.LocalTracer;
import io.crnk.core.engine.dispatcher.RepositoryRequestSpec;
import io.crnk.core.engine.filter.RepositoryFilterBase;
import io.crnk.core.engine.filter.RepositoryFilterContext;
import io.crnk.core.engine.filter.RepositoryRequestFilterChain;
import io.crnk.core.module.Module;
import io.crnk.core.module.Module.ModuleContext;
import io.crnk.core.repository.response.JsonApiResponse;

/**
 * Performs a local trace for each repository call. Keep in mind that a single HTTP request
 * can trigger multiple repository calls if inclusions of relations are in use. .
 */
public class BraveRepositoryFilter extends RepositoryFilterBase {

	public static final String STRING_EXCEPTION = "EXCEPTION";

	public static final String STRING_OK = "OK";

	public static final String QUERY_RESULTS = "crnk.results";

	public static final String STATUS_CODE_ANNOTATION = "crnk.status";

	private static final String QUERY_ANNOTATION = "crnk.query";

	protected static final String COMPONENT_NAME = "crnk";

	protected static final Object COMPONENT_NAME_SEPARATOR = ":";

	private Brave brave;

	private ModuleContext moduleContext;

	public BraveRepositoryFilter(Brave brave, Module.ModuleContext context) {
		this.brave = brave;
		this.moduleContext = context;
	}

	@Override
	public JsonApiResponse filterRequest(RepositoryFilterContext context, RepositoryRequestFilterChain chain) {
		long s = System.nanoTime();

		LocalTracer localTracer = brave.localTracer();
		RepositoryRequestSpec request = context.getRequest();

		String componentName = BraveUtil.getComponentName(request);
		String query = BraveUtil.getQuery(request, moduleContext.getResourceRegistry());

		localTracer.startNewSpan(COMPONENT_NAME, componentName);

		JsonApiResponse result = null;
		Exception exception = null;
		try {
			result = chain.doFilter(context);
			return result;
		}
		catch (RuntimeException e) {
			exception = e;
			throw e;
		}
		finally {
			boolean resultError = result != null && result.getErrors() != null && result.getErrors().iterator().hasNext();
			boolean failed = exception != null || resultError;
			long duration = (System.nanoTime() - s) / 1000;
			String status = failed ? STRING_EXCEPTION : STRING_OK;

			localTracer.submitBinaryAnnotation(STATUS_CODE_ANNOTATION, status);
			writeQuery(localTracer, query);
			writeResults(localTracer, result);
			localTracer.finishSpan(duration);
		}
	}

	private void writeQuery(LocalTracer localTracer, String query) {
		if (query != null) {
			localTracer.submitBinaryAnnotation(QUERY_ANNOTATION, query);
		}
	}

	private void writeResults(LocalTracer localTracer, JsonApiResponse result) {
		if (result != null && result.getEntity() != null) {
			int numResults = getResultCount(result);
			localTracer.submitBinaryAnnotation(QUERY_RESULTS, Integer.toString(numResults));
		}
	}

	private int getResultCount(JsonApiResponse result) {
		return result.getEntity() instanceof Collection ? ((Collection<?>) result.getEntity()).size() : 1;
	}
}
