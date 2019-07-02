package io.crnk.core.engine.security;

import io.crnk.core.engine.result.Result;

public interface SecurityProvider {

	Result<Boolean> isUserInRole(String role);

	/**
	 * @return true if the user has been logged in. If not, a {@link io.crnk.core.exception.UnauthorizedException} rather than
	 * {@link io.crnk.core.exception.ForbiddenException} is  thrown.
	 */
	boolean isAuthenticated();
}
