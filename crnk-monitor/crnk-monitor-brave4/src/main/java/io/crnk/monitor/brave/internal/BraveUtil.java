package io.crnk.monitor.brave.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.core.engine.dispatcher.RepositoryRequestSpec;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.internal.utils.StringUtils;
import io.crnk.core.engine.parser.TypeParser;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.queryspec.internal.QuerySpecAdapter;
import io.crnk.core.queryspec.mapper.DefaultQuerySpecUrlMapper;
import io.crnk.core.queryspec.mapper.QuerySpecUrlContext;
import io.crnk.core.queryspec.mapper.UrlBuilder;

import java.util.Map;
import java.util.Set;

public class BraveUtil {

    private BraveUtil() {
    }


    public static String getQuery(RepositoryRequestSpec request, ResourceRegistry resourceRegistry, TypeParser typeParser, ObjectMapper objectMapper,
								  UrlBuilder urlBuilder) {
        QueryAdapter queryAdapter = request.getQueryAdapter();
        StringBuilder builder = new StringBuilder();
        builder.append("?");
        if (queryAdapter instanceof QuerySpecAdapter) {
            QuerySpec querySpec = request.getQuerySpec(queryAdapter.getResourceInformation());
            DefaultQuerySpecUrlMapper serializer = new DefaultQuerySpecUrlMapper();
            serializer.init(new QuerySpecUrlContext() {
                @Override
                public ResourceRegistry getResourceRegistry() {
                    return resourceRegistry;
                }

                @Override
                public TypeParser getTypeParser() {
                    return typeParser;
                }

                @Override
                public ObjectMapper getObjectMapper() {
                    return objectMapper;
                }

				@Override
				public UrlBuilder getUrlBuilder() {
					return urlBuilder;
				}
			});
            Map<String, Set<String>> parameters = serializer.serialize(querySpec, request.getQueryAdapter().getQueryContext());
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

    public static String getSpanName(RepositoryRequestSpec request) {
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
        } else {
            pathBuilder.append(relationshipField.getResourceInformation().getResourceType());
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
