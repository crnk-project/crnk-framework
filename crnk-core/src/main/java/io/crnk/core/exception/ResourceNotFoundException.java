package io.crnk.core.exception;

import io.crnk.core.engine.document.ErrorData;
import io.crnk.core.engine.http.HttpStatus;

/**
 * Thrown when resource for a type cannot be found.
 */
public final class ResourceNotFoundException extends CrnkMappableException {

	public ResourceNotFoundException(String message) {
		super(HttpStatus.NOT_FOUND_404, ErrorData.builder().setTitle(message).setDetail(message)
				.setStatus(String.valueOf(HttpStatus.NOT_FOUND_404)).build());
	}

}