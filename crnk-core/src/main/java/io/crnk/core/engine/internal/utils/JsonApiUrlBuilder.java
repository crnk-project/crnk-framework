package io.crnk.core.engine.internal.utils;

import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.query.QueryContext;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.module.ModuleRegistry;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.queryspec.internal.QuerySpecAdapter;
import io.crnk.core.queryspec.mapper.QuerySpecUrlMapper;
import io.crnk.legacy.queryParams.QueryParams;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Callable;

public class JsonApiUrlBuilder {

    private final QueryContext queryContext;

    private final ModuleRegistry moduleRegistry;


    public JsonApiUrlBuilder(ModuleRegistry moduleRegistry, QueryContext queryContext) {
        this.queryContext = queryContext;
        this.moduleRegistry = moduleRegistry;
    }

    public String buildUrl(ResourceInformation resourceInformation, Object id, QueryParams queryParams) {
        return buildUrl(resourceInformation, id, queryParams, null);
    }

    public String buildUrl(ResourceInformation resourceInformation, Object id, QuerySpec querySpec) {
        return buildUrl(resourceInformation, id, querySpec, null);
    }

    public String buildUrl(ResourceInformation resourceInformation, Object id, QueryAdapter queryAdapter,
                           String relationshipName) {
        return buildUrl(resourceInformation, id, ((QuerySpecAdapter) queryAdapter).getQuerySpec(), relationshipName);
    }

    public String buildUrl(ResourceInformation resourceInformation, Object id, QuerySpec querySpec, String relationshipName) {
        return buildUrlInternal(resourceInformation, id, querySpec, relationshipName, true);
    }

    public String buildUrl(ResourceInformation resourceInformation, Object id, QuerySpec querySpec, String relationshipName, boolean selfLink) {
        return buildUrlInternal(resourceInformation, id, querySpec, relationshipName, selfLink);
    }

    public String buildUrl(ResourceInformation resourceInformation, Object id, QueryParams queryParams, String relationshipName) {
        return buildUrlInternal(resourceInformation, id, queryParams, relationshipName, true);
    }

    private String buildUrlInternal(ResourceInformation resourceInformation, Object id, Object query, String relationshipName, boolean selfLink) {
        String url;
        ResourceRegistry resourceRegistry = moduleRegistry.getResourceRegistry();
        if (id instanceof Collection) {
            if (resourceInformation.isNested()) {
                throw new UnsupportedOperationException("not yet implemented");
            }
            url = resourceRegistry.getResourceUrl(queryContext, resourceInformation);
            Collection<?> ids = (Collection<?>) id;
            Collection<String> strIds = new ArrayList<>();
            for (Object idElem : ids) {
                String strIdElem = resourceInformation.toIdString(idElem);
                strIds.add(strIdElem);
            }
            url += "/";
            url += StringUtils.join(",", strIds);
        } else if (id != null) {
            url = resourceRegistry.getResourceUrl(queryContext, resourceInformation, id);
        } else {
            url = resourceRegistry.getResourceUrl(queryContext, resourceInformation);
        }
        if (relationshipName != null && selfLink) {
            url += "/relationships/" + relationshipName;
        } else if (relationshipName != null) {
            url += "/" + relationshipName;
        }

        UrlParameterBuilder urlBuilder = new UrlParameterBuilder(url);

        QuerySpec querySpec = (QuerySpec) query;
        QuerySpecUrlMapper urlMapper = moduleRegistry.getUrlMapper();
        urlBuilder.addQueryParameters(urlMapper.serialize(querySpec));

        return urlBuilder.toString();
    }

    class UrlParameterBuilder {

        private StringBuilder builder = new StringBuilder();

        private boolean firstParam;

        private String encoding = "UTF-8";

        public UrlParameterBuilder(String baseUrl) {
            builder.append(baseUrl);
            firstParam = !baseUrl.contains("?");
        }

        @Override
        public String toString() {
            return builder.toString();
        }

        private void addQueryParameters(Map<String, ?> params) {
            if (params != null && !params.isEmpty()) {
                for (Map.Entry<String, ?> entry : params.entrySet()) {
                    String key = entry.getKey();
                    Object value = entry.getValue();
                    addQueryParameter(key, value);
                }
            }
        }

        public void addQueryParameter(String key, final String value) {
            if (firstParam) {
                builder.append("?");
                firstParam = false;
            } else {
                builder.append("&");
            }
            builder.append(key);
            builder.append("=");
            ExceptionUtil.wrapCatchedExceptions(new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    builder.append(URLEncoder.encode(value, encoding));
                    return null;
                }
            });
        }

        private void addQueryParameter(String key, Object value) {
            if (value instanceof Collection) {
                for (Object element : (Collection<?>) value) {
                    addQueryParameter(key, (String) element);
                }
            } else {
                addQueryParameter(key, (String) value);
            }
        }
    }
}
