package io.crnk.rs.internal;

import io.crnk.core.engine.security.SecurityProvider;

import javax.ws.rs.core.SecurityContext;

public class JaxrsSecurityProvider implements SecurityProvider {

	private SecurityContext context;

	public JaxrsSecurityProvider(SecurityContext context) {
		this.context = context;
	}

	@Override
	public boolean isUserInRole(String role) {
		return context.isUserInRole(role);
	}
}
