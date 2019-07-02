package io.crnk.rs.internal;

import javax.ws.rs.core.SecurityContext;

import io.crnk.core.engine.result.ImmediateResult;
import io.crnk.core.engine.security.SecurityProvider;
import io.crnk.core.engine.security.SecurityProviderContext;

public class JaxrsSecurityProvider implements SecurityProvider {

	private SecurityContext context;

	public JaxrsSecurityProvider(SecurityContext context) {
		this.context = context;
	}

	@Override
	public boolean isUserInRole(String role, SecurityProviderContext context) {
		return this.context.isUserInRole(role);
	}

	@Override
	public boolean isAuthenticated() {
		return context.getUserPrincipal() != null;
	}
}
