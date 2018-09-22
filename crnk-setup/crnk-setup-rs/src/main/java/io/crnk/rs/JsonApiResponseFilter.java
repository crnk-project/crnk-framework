package io.crnk.rs;

import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.http.HttpRequestContext;
import io.crnk.core.engine.http.HttpRequestContextProvider;
import io.crnk.core.engine.internal.document.mapper.DocumentMapper;
import io.crnk.core.engine.internal.document.mapper.DocumentMappingConfig;
import io.crnk.core.engine.internal.http.HttpRequestContextBaseAdapter;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.queryspec.internal.QuerySpecAdapter;
import io.crnk.core.repository.response.JsonApiResponse;
import io.crnk.core.resource.list.ResourceListBase;
import io.crnk.core.utils.Nullable;
import io.crnk.rs.type.JsonApiMediaType;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

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
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
		Object response = responseContext.getEntity();
		if (response == null) {
			if (feature.getBoot().isNullDataResponseEnabled()) {
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
			HttpRequestContextProvider httpRequestContextProvider = boot.getModuleRegistry().getHttpRequestContextProvider();
			try {
				HttpRequestContext context = new HttpRequestContextBaseAdapter(new JaxrsRequestContext(requestContext,
						feature));
				httpRequestContextProvider.onRequestStarted(context);

				JsonApiResponse jsonApiResponse = new JsonApiResponse();
				jsonApiResponse.setEntity(response);
				// use the Crnk document mapper to create a JSON API response
				DocumentMappingConfig mappingConfig = new DocumentMappingConfig();
				QueryAdapter queryAdapter = new QuerySpecAdapter(null, null, context.getQueryContext());
				responseContext.setEntity(documentMapper.toDocument(jsonApiResponse, queryAdapter, mappingConfig).get());
				responseContext.getHeaders().put("Content-Type",
						Collections.singletonList((Object) JsonApiMediaType.APPLICATION_JSON_API));
			} finally {
				httpRequestContextProvider.onRequestFinished();
			}
		} else if (isJsonApiResponse(responseContext) && !doNotWrap(response)) {
			Document document = new Document();
			document.setData(Nullable.of(response));
			responseContext.setEntity(document);
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