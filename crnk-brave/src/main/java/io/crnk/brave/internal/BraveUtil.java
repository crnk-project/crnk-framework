package io.crnk.brave.internal;

import java.util.Map;
import java.util.Set;

import io.crnk.core.engine.dispatcher.RepositoryRequestSpec;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.internal.utils.StringUtils;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.queryspec.DefaultQuerySpecSerializer;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.queryspec.internal.QuerySpecAdapter;

public class BraveUtil {

	private BraveUtil() {
	}


	public static String getQuery(RepositoryRequestSpec request, ResourceRegistry resourceRegistry) {
		QueryAdapter queryAdapter = request.getQueryAdapter();
		StringBuilder builder = new StringBuilder();
		builder.append("?");
		if (queryAdapter instanceof QuerySpecAdapter) {
			QuerySpec querySpec = request.getQuerySpec(queryAdapter.getResourceInformation());
			DefaultQuerySpecSerializer serializer = new DefaultQuerySpecSerializer(resourceRegistry);
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

	public static String getComponentName(RepositoryRequestSpec request) {
		ResourceField relationshipField = request.getRelationshipField();
		StringBuilder pathBuilder = new StringBuilder();
		String method = request.getMethod().toString();
		pathBuilder.append(BraveRepositoryFilter.COMPONENT_NAME);
		pathBuilder.append(BraveRepositoryFilter.COMPONENT_NAME_SEPARATOR);
		pathBuilder.append(method);
		pathBuilder.append(BraveRepositoryFilter.COMPONENT_NAME_SEPARATOR);
		pathBuilder.append("/");

		if (relationshipField == null) {
			pathBuilder.append(request.getQueryAdapter().getResourceInformation().getResourceType());
		}
		else {
			pathBuilder.append(relationshipField.getParentResourceInformation().getResourceType());
		}
		pathBuilder.append("/");

		Iterable<Object> ids = request.getIds();
		if (ids != null) {
			pathBuilder.append(StringUtils.join(",", ids));
			pathBuilder.append("/");
		}
		if (relationshipField != null) {
			pathBuilder.append(relationshipField.getJsonName());
			pathBuilder.append("/");
		}
		return pathBuilder.toString();
	}


}
