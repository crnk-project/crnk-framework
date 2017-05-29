package io.crnk.rs;

import java.io.IOException;
import java.util.Arrays;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Response;

import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.http.HttpRequestContext;
import io.crnk.core.engine.http.HttpRequestContextProvider;
import io.crnk.core.engine.internal.dispatcher.HttpRequestContextBaseAdapter;
import io.crnk.core.engine.internal.document.mapper.DocumentMapper;
import io.crnk.core.engine.registry.ResourceRegistry;
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
			// TODO
			if (feature.getBoot().isNullDataResponseEnabled()) {
				Document document = new Document();
				document.setData(Nullable.nullValue());
				responseContext.setEntity(document);
				responseContext.setStatus(Response.Status.OK.getStatusCode());
				responseContext.getHeaders().put("Content-Type", Arrays.asList((Object) JsonApiMediaType.APPLICATION_JSON_API));
			}
			return;
		}

		// only modify responses which contain a single or a list of Crnk resources
		if (isResourceResponse(response)) {
			CrnkBoot boot = feature.getBoot();
			ResourceRegistry resourceRegistry = boot.getResourceRegistry();
			DocumentMapper documentMapper = boot.getDocumentMapper();

			ServiceUrlProvider serviceUrlProvider = resourceRegistry.getServiceUrlProvider();
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
				responseContext.getHeaders().put("Content-Type", Arrays.asList((Object) JsonApiMediaType.APPLICATION_JSON_API));

			}
			finally {
				if (serviceUrlProvider instanceof HttpRequestContextProvider) {
					((HttpRequestContextProvider) serviceUrlProvider).onRequestFinished();
				}
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
}