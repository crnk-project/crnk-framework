package io.crnk.rs;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.ws.rs.Produces;
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
import io.crnk.core.engine.internal.document.mapper.DocumentMapper;
import io.crnk.core.engine.internal.http.HttpRequestContextBaseAdapter;
import io.crnk.core.engine.url.ServiceUrlProvider;
import io.crnk.core.repository.response.JsonApiResponse;
import io.crnk.core.resource.list.ResourceListBase;
import io.crnk.core.utils.Nullable;
import io.crnk.rs.type.JsonApiMediaType;

/**
 * Uses the Crnk {@link DocumentMapper} to create a JSON API response for
 * custom JAX-RS actions returning Crnk resources.
 */
public class JsonApiResponseFilter implements ContainerResponseFilter {

	private CrnkFeature feature;

	private boolean jsonApiByDefault = true;

	@Context
	private ResourceInfo resourceInfo;

	public JsonApiResponseFilter(CrnkFeature feature) {
		this.feature = feature;
	}

	public JsonApiResponseFilter(CrnkFeature feature, boolean jsonApiByDefault) {
		this.feature = feature;
		this.jsonApiByDefault = jsonApiByDefault;
	}

	/**
	 * Creates JSON API responses for custom JAX-RS actions returning Crnk resources.
	 */
	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
		Object response = responseContext.getEntity();
		if (response == null) {
			if (isJsonApiResponse(resourceInfo) && feature.getBoot().isNullDataResponseEnabled()) {
				Document document = new Document();
				document.setData(Nullable.nullValue());
				responseContext.setEntity(document);
				responseContext.setStatus(Response.Status.OK.getStatusCode());
				responseContext.getHeaders().put("Content-Type",
						Collections.singletonList((Object) JsonApiMediaType.APPLICATION_JSON_API));
			}
			return;
		}

		// only modify responses which contain a single or a list of Crnk resources
		if (isResourceResponse(response)) {
			CrnkBoot boot = feature.getBoot();
			DocumentMapper documentMapper = boot.getDocumentMapper();
			ServiceUrlProvider serviceUrlProvider = boot.getServiceUrlProvider();
			try {
				if (serviceUrlProvider instanceof HttpRequestContextProvider) {
					HttpRequestContext context = new HttpRequestContextBaseAdapter(new JaxrsRequestContext(requestContext,
							feature));
					((HttpRequestContextProvider) serviceUrlProvider).onRequestStarted(context);
				}

				JsonApiResponse jsonApiResponse = new JsonApiResponse();
				jsonApiResponse.setEntity(response);
				// use the Crnk document mapper to create a JSON API response
				responseContext.setEntity(documentMapper.toDocument(jsonApiResponse, null));
				responseContext.getHeaders().put("Content-Type",
						Collections.singletonList((Object) JsonApiMediaType.APPLICATION_JSON_API));
			}
			finally {
				if (serviceUrlProvider instanceof HttpRequestContextProvider) {
					((HttpRequestContextProvider) serviceUrlProvider).onRequestFinished();
				}
			}
		}
		else if (isJsonApiResponse(responseContext) && !doNotWrap(response)) {
			Document document = new Document();
			document.setData(Nullable.of(response));
			responseContext.setEntity(document);
			if (jsonApiByDefault) {
				responseContext.getHeaders().put("Content-Type",
						Collections.singletonList((Object) JsonApiMediaType.APPLICATION_JSON_API));
			}
		}
	}

	/**
	 * Determines whether the given response entity is either a Crnk
	 * document or a list of Crnk resources.
	 *
	 * @param response the response entity
	 * @return <code>true</code>, if <code>response</code> is a (list of)
	 * Crnk document(s),<br />
	 * <code>false</code>, otherwise
	 */
	private boolean isResourceResponse(Object response) {
		boolean singleResource = feature.getBoot().getResourceRegistry().hasEntry(response.getClass());
		boolean resourceList = ResourceListBase.class.isAssignableFrom(response.getClass());
		return singleResource || resourceList;
	}

	/**
	 * Retrieves the called resource method from the passed <code>resourceInfo</code> to
	 * determine whether it has a <code>@Produces</code> annotation specifying the JSON-API media type.
	 *
	 * @param resourceInfo the resource information for the currently processed request
	 * @return <code>true</code>, if the called resource method produces JSON-API,<br />
	 * 			<code>false</code>, otherwise
	 */
	private boolean isJsonApiResponse(ResourceInfo resourceInfo) {
		Method method = resourceInfo.getResourceMethod();
		return jsonApiByDefault || hasProducesJsonApiAnnotation(method, null);
	}

	/**
	 * Checks the annotations of the passed <code>method</code>, and if necessary the super interface(s) to
	 * determine whether it has a <code>@Produces</code> annotation specifying the JSON-API media type.
	 *
	 * @param method the method whose annotations should be checked
	 * @param previous the class which contains the method to check
	 * @return <code>true</code>, if the passed method produces JSON-API,<br />
	 * 			<code>false</code>, otherwise
	 */
	private boolean hasProducesJsonApiAnnotation(Method method, Class<?> previous) {
		Produces produces = method.getAnnotation(Produces.class);
		if (produces == null) {
			// check if the method is declared in an interface
			Class<?>[] interfaces = resourceInfo.getResourceClass().getInterfaces();
			for (Class<?> intf : interfaces) {
				if (!intf.equals(previous)) {
					try {
						Method interfaceMethod = intf.getDeclaredMethod(method.getName(), method.getParameterTypes());
						return hasProducesJsonApiAnnotation(interfaceMethod, intf);
					}
					catch (NoSuchMethodException e) {
						// ignore and continue
					}
				}
			}
		}
		else {
			List<String> value = Arrays.asList(produces.value());
			return value.contains(JsonApiMediaType.APPLICATION_JSON_API);

		}
		return false;
	}

	/**
	 * Reads the media type from the response context and compares it against {@link JsonApiMediaType#APPLICATION_JSON_API}.
	 *
	 * @param responseContext the container response context for the current request
	 * @return <code>true</code>, if the requested method returns JSON-API,<br />
	 * 			<code>false</code>, otherwise
	 */
	private boolean isJsonApiResponse(ContainerResponseContext responseContext) {
		return jsonApiByDefault || JsonApiMediaType.APPLICATION_JSON_API_TYPE.equals(responseContext.getMediaType());
	}

	/**
	 * Some entity objects cannot be wrapped in a {@link Document} object. These include
	 * <ul>
	 *     <li>{@link Document}, and</li>
	 *     <li>{@link InputStream}</li>
	 * </ul>
	 *
	 * @param entity the container response context's entity object
	 * @return <code>true</code>, if the response is of one of the aforementioned types and should thus not be wrapped,<br />
	 * 			<code>false</code>, otherwise
	 */
	private boolean doNotWrap(Object entity) {
		return entity instanceof Document || entity instanceof InputStream;
	}

}