package io.crnk.rs.internal;

import javax.ws.rs.core.SecurityContext;

import io.crnk.core.engine.security.SecurityProvider;

public class JaxrsSecurityProvider implements SecurityProvider {

	private SecurityContext context;

	public JaxrsSecurityProvider(SecurityContext context) {
		this.context = context;
	}

	@Override
	public boolean isUserInRole(String role) {
		return context.isUserInRole(role);
	}

	@Override
	public boolean isAuthenticated() {
		return context.getUserPrincipal() != null;
	}
}
