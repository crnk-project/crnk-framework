package io.crnk.core.engine.internal.dispatcher.controller;

import io.crnk.core.engine.dispatcher.Response;
import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.document.Resource;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.http.HttpStatus;
import io.crnk.core.engine.http.HttpStatusBehavior;
import io.crnk.core.engine.http.HttpStatusContext;
import io.crnk.core.engine.internal.dispatcher.path.JsonPath;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.engine.result.Result;
import io.crnk.core.exception.BadRequestException;
import io.crnk.core.exception.RepositoryNotFoundException;
import io.crnk.core.exception.RequestBodyException;
import io.crnk.core.exception.RequestBodyNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

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
	 * @param jsonPath Requested resource path
	 * @param queryAdapter QueryAdapter
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

	protected void validateCreatedResponse(Response response) {
		Integer httpStatus = response.getHttpStatus();
		Document document = response.getDocument();
		if (httpStatus == HttpStatus.CREATED_201) {
			if (!document.getData().isPresent()) {
				throw new IllegalStateException("upon POST with status 201 a resource must be returned");
			}
			for (Resource resource : document.getCollectionData().get()) {
				if (resource.getId() == null) {
					throw new IllegalStateException("upon POST with status 201 the resource must have an ID, consider 202 otherwise");
				}
			}
		}
	}

	protected List<Resource> getRequestBodys(Document requestDocument, JsonPath path, HttpMethod method) {
		String resourcePath = path.getRootEntry().getResourceInformation().getResourcePath();

		assertRequestDocument(requestDocument, method, resourcePath);

		if (!requestDocument.getData().isPresent() || requestDocument.getData().get() == null) {
			throw new RequestBodyException(method, resourcePath, "No data field in the body.");
		}

		Object data = requestDocument.getData().get();
		List<Resource> resourceBodies = data instanceof List ? (List<Resource>) data : Arrays.asList((Resource) data);
		for (Resource resourceBody : resourceBodies) {
			verifyResourceBody(resourceBody, path);
		}
		return resourceBodies;
	}

	protected void verifyResourceBody(Resource resourceBody, JsonPath path) {
		assignDefaultType(resourceBody, path);
		String resourceType = resourceBody.getType();
		RegistryEntry bodyRegistryEntry = context.getResourceRegistry().getEntry(resourceType);
		if (bodyRegistryEntry == null) {
			throw new RepositoryNotFoundException(resourceType);
		}
	}

	protected void assertRequestDocument(Document requestDocument, HttpMethod method, String resourceType) {
		if (requestDocument == null) {
			throw new RequestBodyNotFoundException(method, resourceType);
		}
	}

	private void assignDefaultType(Resource resourceBody, JsonPath path) {
		String type = resourceBody.getType();
		if (type == null && path.getParentField() != null) {
			resourceBody.setType(path.getParentField().getOppositeResourceType());
		} else if (type == null) {
			resourceBody.setType(path.getRootEntry().getResourceInformation().getResourceType());
		}
	}

	protected int getStatus(Document responseDocument, HttpMethod method) {
		HttpStatusBehavior statusBehavior = context.getHttpStatusBehavior();

		return statusBehavior.getStatus(new HttpStatusContext() {

			@Override
			public Document getResponseDocument() {
				return responseDocument;
			}

			@Override
			public HttpMethod getMethod() {
				return method;
			}
		});

	}
}
