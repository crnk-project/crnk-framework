package io.crnk.client;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.MultivaluedMap;
import java.io.IOException;

/**
 * A JAX-RS {@link ContainerRequestFilter} that helps gather information
 * about calls into the server for testing purposes.
 *
 * @author Craig Setera
 */
@PreMatching
public class TestRequestFilter implements ContainerRequestFilter {

	private MultivaluedMap<String, String> lastReceivedHeaders;

	/**
	 * Clear the last received headers value.
	 */
	public void clearLastReceivedHeaders() {
		this.lastReceivedHeaders = null;
	}

	/*
	 * (non-Javadoc)
	 * @see jakarta.ws.rs.container.ContainerRequestFilter#filter(jakarta.ws.rs.container.ContainerRequestContext)
	 */
	@Override
	public void filter(ContainerRequestContext requestContext) {
		this.lastReceivedHeaders = requestContext.getHeaders();
	}

	/**
	 * Return the last received headers from the client.
	 *
	 * @return
	 */
	public MultivaluedMap<String, String> getLastReceivedHeaders() {
		return lastReceivedHeaders;
	}
}
