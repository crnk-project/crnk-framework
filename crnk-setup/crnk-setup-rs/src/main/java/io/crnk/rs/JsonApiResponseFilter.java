package io.crnk.rs;

import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.http.HttpRequestContext;
import io.crnk.core.engine.http.HttpRequestContextProvider;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.document.mapper.DocumentMapper;
import io.crnk.core.engine.internal.document.mapper.DocumentMappingConfig;
import io.crnk.core.engine.internal.http.HttpRequestContextBaseAdapter;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.query.QueryContext;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.queryspec.internal.QuerySpecAdapter;
import io.crnk.core.queryspec.mapper.QueryParameterType;
import io.crnk.core.queryspec.mapper.QuerySpecUrlMapper;
import io.crnk.core.repository.response.JsonApiResponse;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.core.utils.Nullable;
import io.crnk.rs.type.JsonApiMediaType;

/**
 * Uses the Crnk {@link DocumentMapper} to create a JSON API response for
 * custom JAX-RS actions returning Crnk resources.
 */
public class JsonApiResponseFilter implements ContainerResponseFilter {

    private CrnkFeature feature;

    @Context
    private ResourceInfo resourceInfo;

    public JsonApiResponseFilter(CrnkFeature feature) {
        this.feature = feature;
    }

    /**
     * Creates JSON API responses for custom JAX-RS actions returning Crnk resources.
     */
    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        Object response = responseContext.getEntity();
        if (response == null) {
            if (feature.getBoot().isNullDataResponseEnabled()) {
                Document document = new Document();
                document.setData(Nullable.nullValue());
                responseContext.setEntity(document);
                responseContext.setStatus(Response.Status.OK.getStatusCode());
                responseContext.getHeaders().put("Content-Type",
                        Collections.singletonList(JsonApiMediaType.APPLICATION_JSON_API));
            }
            return;
        }

        // only modify responses which contain a single or a list of Crnk resources
        Optional<RegistryEntry> registryEntry = getRegistryEntry(response);
        if (registryEntry.isPresent()) {
            CrnkBoot boot = feature.getBoot();
            DocumentMapper documentMapper = boot.getDocumentMapper();
            HttpRequestContextProvider httpRequestContextProvider = boot.getModuleRegistry().getHttpRequestContextProvider();
            try {
                HttpRequestContext context = new HttpRequestContextBaseAdapter(new JaxrsRequestContext(requestContext, feature));
                httpRequestContextProvider.onRequestStarted(context);

                JsonApiResponse jsonApiResponse = new JsonApiResponse();
                jsonApiResponse.setEntity(response);
                // use the Crnk document mapper to create a JSON API response
                DocumentMappingConfig mappingConfig = new DocumentMappingConfig();

                ResourceInformation resourceInformation = registryEntry.get().getResourceInformation();

                Map<String, Set<String>> jsonApiParameters = context.getRequestParameters().entrySet()
                        .stream()
                        .filter(entry -> isJsonApiParameter(entry.getKey()))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                QuerySpecUrlMapper urlMapper = feature.getBoot().getUrlMapper();
                QueryContext queryContext = context.getQueryContext();
                QuerySpec querySpec = urlMapper.deserialize(resourceInformation, jsonApiParameters, queryContext);

                ResourceRegistry resourceRegistry = feature.getBoot().getResourceRegistry();
                QueryAdapter queryAdapter = new QuerySpecAdapter(querySpec, resourceRegistry, queryContext);
                responseContext.setEntity(documentMapper.toDocument(jsonApiResponse, queryAdapter, mappingConfig).get());
                responseContext.getHeaders().put("Content-Type",
                        Collections.singletonList(JsonApiMediaType.APPLICATION_JSON_API));
            } finally {
                httpRequestContextProvider.onRequestFinished();
            }
        } else if (isJsonApiResponse(responseContext) && !doNotWrap(response)) {
            Document document = new Document();
            document.setData(Nullable.of(response));
            responseContext.setEntity(document);
        }
    }

    private boolean isJsonApiParameter(String key) {
        for (QueryParameterType type : QueryParameterType.values()) {
            if (key.startsWith(type.toString().toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines whether the given response entity is either a Crnk
     * resource or a list of resource;
     */
    private Optional<RegistryEntry> getRegistryEntry(Object response) {
        if (response != null) {
            Class responseClass = response.getClass();
            boolean resourceList = ResourceList.class.isAssignableFrom(responseClass);
            if (resourceList) {
                ResourceList responseList = (ResourceList) response;
                if (responseList.isEmpty()) {
                    return Optional.empty();
                }

                // get common super class of all list element => resource class
                Class elementType = responseList.get(0).getClass();
                for (int i = 0; i < responseList.size(); i++) {
                    Class otherType = responseList.get(i).getClass();
                    while (!elementType.isAssignableFrom(otherType)) {
                        elementType = elementType.getSuperclass();
                    }
                }
                responseClass = elementType;
            }

            ResourceRegistry resourceRegistry = feature.getBoot().getResourceRegistry();
            if (resourceRegistry.hasEntry(responseClass)) {
                return Optional.of(resourceRegistry.getEntry(responseClass));
            }
        }
        return Optional.empty();
    }

    /**
     * Reads the media type from the response context and compares it against {@link JsonApiMediaType#APPLICATION_JSON_API}.
     *
     * @param responseContext the container response context for the current request
     * @return <code>true</code>, if the requested method returns JSON-API,<br />
     * <code>false</code>, otherwise
     */
    private boolean isJsonApiResponse(ContainerResponseContext responseContext) {
        return JsonApiMediaType.APPLICATION_JSON_API_TYPE.equals(responseContext.getMediaType());
    }

    /**
     * Some entity objects cannot be wrapped in a {@link Document} object. These include
     * <ul>
     * <li>{@link Document}, and</li>
     * <li>{@link InputStream}</li>
     * </ul>
     *
     * @param entity the container response context's entity object
     * @return <code>true</code>, if the response is of one of the aforementioned types and should thus not be wrapped,<br />
     * <code>false</code>, otherwise
     */
    private boolean doNotWrap(Object entity) {
        return entity instanceof Document || entity instanceof InputStream;
    }

}