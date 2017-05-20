package io.crnk.brave.internal;

import com.github.kristofa.brave.Brave;
import com.github.kristofa.brave.LocalTracer;
import io.crnk.core.engine.dispatcher.RepositoryRequestSpec;
import io.crnk.core.engine.filter.RepositoryFilterBase;
import io.crnk.core.engine.filter.RepositoryFilterContext;
import io.crnk.core.engine.filter.RepositoryRequestFilterChain;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.internal.utils.StringUtils;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.module.Module;
import io.crnk.core.module.Module.ModuleContext;
import io.crnk.core.queryspec.DefaultQuerySpecSerializer;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.queryspec.internal.QuerySpecAdapter;
import io.crnk.core.repository.response.JsonApiResponse;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Performs a local trace for each repository call. Keep in mind that a single HTTP request
 * can trigger multiple repository calls if inclusions of relations are in use. .
 */
public class BraveRepositoryFilter extends RepositoryFilterBase {

	public static final String STRING_EXCEPTION = "EXCEPTION";

	public static final String STRING_OK = "OK";
	public static final String QUERY_RESULTS = "crnk.results";
	private static final String STATUS_CODE_ANNOTATION = "crnk.status";
	private static final String QUERY_ANNOTATION = "crnk.query";

	private static final String COMPONENT_NAME = "crnk";

	private static final Object COMPONENT_NAME_SEPARATOR = ":";

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

		String componentName = getComponentName(request);
		String query = getQuery(request);

		localTracer.startNewSpan(COMPONENT_NAME, componentName);

		JsonApiResponse result = null;
		Exception exception = null;
		try {
			result = chain.doFilter(context);
			return result;
		} catch (RuntimeException e) {
			exception = e;
			throw e;
		} finally {
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

	private String getQuery(RepositoryRequestSpec request) {
		QueryAdapter queryAdapter = request.getQueryAdapter();
		StringBuilder builder = new StringBuilder();
		builder.append("?");
		if (queryAdapter instanceof QuerySpecAdapter) {
			QuerySpec querySpec = request.getQuerySpec(queryAdapter.getResourceInformation());
			DefaultQuerySpecSerializer serializer = new DefaultQuerySpecSerializer(moduleContext.getResourceRegistry());
			Map<String, Set<String>> parameters = serializer.serialize(querySpec);
			for (Map.Entry<String, Set<String>> entry : parameters.entrySet()) {
				if (builder.length() > 1) {
					builder.append("&");
				}
				builder.append(entry.getKey());
				builder.append("=");
				builder.append(StringUtils.join(",", entry.getValue()));
			}
			return builder.toString();
		}
		return null;
	}

	private String getComponentName(RepositoryRequestSpec request) {
		ResourceField relationshipField = request.getRelationshipField();
		StringBuilder pathBuilder = new StringBuilder();
		String method = request.getMethod().toString();
		pathBuilder.append(COMPONENT_NAME);
		pathBuilder.append(COMPONENT_NAME_SEPARATOR);
		pathBuilder.append(method);
		pathBuilder.append(COMPONENT_NAME_SEPARATOR);
		pathBuilder.append("/");

		if (relationshipField == null) {
			pathBuilder.append(request.getQueryAdapter().getResourceInformation().getResourceType());
		} else {
			pathBuilder.append(relationshipField.getParentResourceInformation().getResourceType());
		}
		pathBuilder.append("/");

		Iterable<Object> ids = request.getIds();
		if (ids != null) {
			Iterator<Object> iterator = ids.iterator();
			pathBuilder.append(iterator.next());
			while (iterator.hasNext()) {
				pathBuilder.append(",");
				pathBuilder.append(iterator.next());
			}
			pathBuilder.append("/");
		}
		if (relationshipField != null) {
			pathBuilder.append(relationshipField.getJsonName());
			pathBuilder.append("/");
		}
		return pathBuilder.toString();
	}

	private String getResourceType(Class<?> resourceClass) {
		RegistryEntry resourceEntry = moduleContext.getResourceRegistry().findEntry(resourceClass);
		return resourceEntry.getResourceInformation().getResourceType();
	}

	private int getResultCount(JsonApiResponse result) {
		return result.getEntity() instanceof Collection ? ((Collection<?>) result.getEntity()).size() : 1;
	}
}
