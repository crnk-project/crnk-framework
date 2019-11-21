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
import io.crnk.core.exception.RequestBodyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	protected void validateUpdatedResponse(Response response) {
		Integer httpStatus = response.getHttpStatus();
		Document document = response.getDocument();
		if (httpStatus == HttpStatus.OK_200) {
			if (!document.getData().isPresent()) {
				throw new IllegalStateException("upon PATCH with status 200 a resource must be returned");
			}
		}
		else if (httpStatus == HttpStatus.ACCEPTED_202) {
			for (Resource resource : document.getCollectionData().get()) {
				if (resource.getId() == null) {
					throw new IllegalStateException("upon PATCH with status 202 the resource must have an ID");
				}
			}
		}
		else if (httpStatus == HttpStatus.NO_CONTENT_204) {
			// TODO: Figure out what we actually want here
			for (Resource resource : document.getCollectionData().get()) {
				if (resource != null) {
					throw new IllegalStateException("upon PATCH with status 204 should not return any resources");
				}
			}
		}
	}

	Object collectionOrSingleton(List<Object> entities) {
		return entities.size() == 1 ? entities.get(0) : entities;
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
