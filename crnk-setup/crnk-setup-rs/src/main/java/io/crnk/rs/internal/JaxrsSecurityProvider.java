package io.crnk.rs.internal;

import javax.ws.rs.core.SecurityContext;

import io.crnk.core.engine.result.ImmediateResult;
import io.crnk.core.engine.result.Result;
import io.crnk.core.engine.security.SecurityProvider;

public class JaxrsSecurityProvider implements SecurityProvider {

	private SecurityContext context;

	public JaxrsSecurityProvider(SecurityContext context) {
		this.context = context;
	}

	@Override
	public Result<Boolean> isUserInRole(String role) {
		return new ImmediateResult<>(context.isUserInRole(role));
	}

	@Override
	public boolean isAuthenticated() {
		return context.getUserPrincipal() != null;
	}
}
