package io.crnk.core.engine.internal.dispatcher.controller;

import io.crnk.core.boot.CrnkProperties;
import io.crnk.core.engine.dispatcher.Response;
import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.filter.FilterBehavior;
import io.crnk.core.engine.filter.ResourceFilterDirectory;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceFieldAccess;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.dispatcher.path.JsonPath;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.engine.properties.PropertiesProvider;
import io.crnk.core.engine.properties.ResourceFieldImmutableWriteBehavior;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.query.QueryContext;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.engine.result.Result;
import io.crnk.core.exception.BadRequestException;
import io.crnk.core.exception.ForbiddenException;
import io.crnk.core.exception.RequestBodyException;
import io.crnk.core.repository.response.JsonApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a controller contract. There can be many kinds of requests that can be send to the framework. The
 * initial process of checking if a request is acceptable is managed by
 * {@link BaseController#isAcceptable(JsonPath, String)} method. If the method returns
 * true, the matched controller is used to handle the request.
 */
public abstract class BaseController implements Controller {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected ControllerContext context;

    @Override
    public void init(ControllerContext context) {
        this.context = context;
    }

    /**
     * Passes the request to controller method.
     *
     * @param jsonPath        Requested resource path
     * @param queryAdapter    QueryAdapter
     * @param requestDocument Top-level JSON object from method's body of the request passed as {@link Document}
     * @return BaseResponseContext object
     * @deprecated in favor of {@link #handleAsync(JsonPath, QueryAdapter, Document)}
     */
    @Override
    @Deprecated
    public final Response handle(JsonPath jsonPath, QueryAdapter queryAdapter, Document requestDocument) {

        Result<Response> response = handleAsync(jsonPath, queryAdapter, requestDocument);
        PreconditionUtil.verify(response != null, "no response by controller provided");
        return response.get();
    }


    protected void verifyTypes(HttpMethod methodType, RegistryEntry endpointRegistryEntry,
                               RegistryEntry bodyRegistryEntry) {
        if (endpointRegistryEntry.equals(bodyRegistryEntry)) {
            return;
        }
        if (bodyRegistryEntry == null || !bodyRegistryEntry.isParent(endpointRegistryEntry)) {
            String message = String.format("Inconsistent type definition between path and body: body type: " +
                    "%s, request type: %s", bodyRegistryEntry.getResourceInformation().getResourceType(), endpointRegistryEntry.getResourceInformation().getResourceType());
            throw new RequestBodyException(methodType, endpointRegistryEntry.getResourceInformation().getResourceType(), message);
        }
    }

    protected RegistryEntry getRegistryEntry(String resource) {
        ResourceRegistry resourceRegistry = context.getResourceRegistry();
        RegistryEntry registryEntry = resourceRegistry.getEntry(resource);
        if (registryEntry == null) {
            throw new BadRequestException(
                    String.format("Invalid resource type: %s", resource)
            );
        }
        return registryEntry;
    }

    protected void validateCreatedResponse(ResourceInformation resourceInformation, JsonApiResponse response) {
        Object entity = response.getEntity();
        logger.debug("posted resource {}", entity);
        if (entity == null) {
            throw new IllegalStateException("upon POST repository for type=" + resourceInformation.getResourceType()
                    + " must return created resource, not allowed to return null");
        }
        Object id = resourceInformation.getId(entity);
        if (id == null) {
            throw new IllegalStateException("created resource must have an id");
        }
    }
}
