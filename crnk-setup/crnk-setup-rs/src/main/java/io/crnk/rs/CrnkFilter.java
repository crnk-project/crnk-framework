package io.crnk.rs;

import io.crnk.core.engine.dispatcher.RequestDispatcher;
import io.crnk.core.engine.internal.utils.UrlUtils;
import io.crnk.rs.type.JsonApiMediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.Priority;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.Response;

/**
 * Handles JSON API requests.
 * <p>
 * Consumes: <i>null</i> | {@link JsonApiMediaType}
 * Produces: {@link JsonApiMediaType}
 * </p>
 * <p>
 * Currently the response is sent using {@link ContainerRequestContext#abortWith(Response)} which might cause
 * problems with Jackson, co the serialization is happening in this filter.
 * </p>
 * <p>
 * To be able to send a request to Crnk it is necessary to provide full media type alongside the request.
 * Wildcards are not accepted.
 * </p>
 */
@PreMatching
@Priority(Integer.MAX_VALUE) // Greatest value is applied last
public class CrnkFilter implements ContainerRequestFilter {

	private static final Logger LOGGER = LoggerFactory.getLogger(CrnkFilter.class);

	private final CrnkFeature feature;

	public CrnkFilter(CrnkFeature feature) {
		this.feature = feature;
	}

	@Override
	public void filter(ContainerRequestContext requestContext) {
		if (feature.getWebPathPrefix() != null) {
			String path = UrlUtils.removeLeadingSlash(requestContext.getUriInfo().getPath());
			if (!path.startsWith(UrlUtils.addTrailingSlash(feature.getWebPathPrefix()))) {
				return;
			}
		}
		try {
			LOGGER.debug("CrnkFilter entered");
			JaxrsRequestContext context = new JaxrsRequestContext(requestContext, feature);
			RequestDispatcher requestDispatcher = feature.getBoot().getRequestDispatcher();
			requestDispatcher.process(context);
			context.checkAbort();
			LOGGER.debug("CrnkFilter exited");
		} catch (WebApplicationException e) {
			LOGGER.error("failed to dispatch request", e);
			throw e;
		} catch (Exception e) {
			LOGGER.error("failed to dispatch request", e);
			throw new WebApplicationException(e);
		}
	}
}



