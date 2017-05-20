package io.crnk.core.exception;

import io.crnk.core.engine.document.ErrorData;
import io.crnk.core.engine.http.HttpStatus;

public class ForbiddenException extends CrnkMappableException {  // NOSONAR exception hierarchy deep but ok

	private static final String TITLE = "FOBIDDEN";

	public ForbiddenException(String message) {
		super(HttpStatus.FORBIDDEN_403, ErrorData.builder().setTitle(TITLE).setDetail(message)
				.setStatus(String.valueOf(HttpStatus.FORBIDDEN_403)).build());
	}
}
