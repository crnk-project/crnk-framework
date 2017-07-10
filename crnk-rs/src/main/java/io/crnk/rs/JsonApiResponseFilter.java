package io.crnk.rs;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Response;

import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.http.HttpRequestContext;
import io.crnk.core.engine.http.HttpRequestContextBase;
import io.crnk.core.engine.http.HttpRequestContextProvider;
import io.crnk.core.engine.internal.document.mapper.DocumentMapper;
import io.crnk.core.engine.internal.http.HttpRequestContextBaseAdapter;
import io.crnk.core.engine.internal.http.JsonApiRequestProcessor;
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

	public JsonApiResponseFilter(CrnkFeature feature) {
		this.feature = feature;
	}

	/**
	 * Creates JSON API responses for custom JAX-RS actions returning Crnk resources.
	 */
	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
		Object response = responseContext.getEntity();
		if (response == null) {
			if (isJsonApiRequest(requestContext) && feature.getBoot().isNullDataResponseEnabled()) {
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
		else if (isJsonApiRequest(requestContext) && !doNotWrap(response)) {
			Document document = new Document();
			document.setData(Nullable.of(response));
			responseContext.setEntity(document);
			responseContext.getHeaders().put("Content-Type",
					Collections.singletonList((Object) JsonApiMediaType.APPLICATION_JSON_API));
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
	 * Uses {@link JsonApiRequestProcessor#isJsonApiRequest(HttpRequestContext)} to determine if the caller
	 * requested a response in JSON-API format.
	 *
	 * @param requestContext the current container request context as received by this filter
	 * @return <code>true</code>, if the caller requested a JSON-API response,<br />
	 *     		<code>false</code>, otherwise
	 */
	private boolean isJsonApiRequest(ContainerRequestContext requestContext) {
		HttpRequestContextBase httpRequestContextBase = new JaxrsRequestContext(requestContext, feature);
		HttpRequestContextBaseAdapter httpRequestContext = new HttpRequestContextBaseAdapter(httpRequestContextBase);
		return JsonApiRequestProcessor.isJsonApiRequest(httpRequestContext);
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