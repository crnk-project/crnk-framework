package io.crnk.spring.cloud.sleuth.internal;

import io.crnk.core.engine.dispatcher.RepositoryRequestSpec;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.internal.utils.StringUtils;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.queryspec.internal.QuerySpecAdapter;
import io.crnk.core.queryspec.mapper.QuerySpecUrlMapper;

import java.util.Map;
import java.util.Set;

// TODO replicated from BraveUtil
public class SleuthUtil {

    private SleuthUtil() {
    }


    public static String getQuery(RepositoryRequestSpec request, QuerySpecUrlMapper urlMapper) {
        QueryAdapter queryAdapter = request.getQueryAdapter();
        StringBuilder builder = new StringBuilder();
        builder.append("?");
        if (queryAdapter instanceof QuerySpecAdapter) {
            QuerySpec querySpec = request.getQuerySpec(queryAdapter.getResourceInformation());
            Map<String, Set<String>> parameters = urlMapper.serialize(querySpec);
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
        pathBuilder.append(SleuthRepositoryFilter.COMPONENT_NAME);
        pathBuilder.append(SleuthRepositoryFilter.COMPONENT_NAME_SEPARATOR);
        pathBuilder.append(method);
        pathBuilder.append(SleuthRepositoryFilter.COMPONENT_NAME_SEPARATOR);
        pathBuilder.append("/");

        if (relationshipField == null) {
            pathBuilder.append(request.getQueryAdapter().getResourceInformation().getResourceType());
        } else {
            pathBuilder.append(relationshipField.getParentResourceInformation().getResourceType());
        }

        Iterable<Object> ids = request.getIds();
        if (ids != null) {
            pathBuilder.append("/");
            pathBuilder.append(StringUtils.join(",", ids));
        }
        if (relationshipField != null) {
            pathBuilder.append("/");
            pathBuilder.append(relationshipField.getJsonName());
        }
        return pathBuilder.toString();
    }


}
