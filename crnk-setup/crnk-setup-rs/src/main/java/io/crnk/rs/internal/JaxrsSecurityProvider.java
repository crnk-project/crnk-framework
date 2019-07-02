package io.crnk.rs.internal;

import io.crnk.core.engine.security.SecurityProvider;
import io.crnk.core.engine.security.SecurityProviderContext;

import javax.ws.rs.core.SecurityContext;

public class JaxrsSecurityProvider implements SecurityProvider {

    private SecurityContext context;

    public JaxrsSecurityProvider(SecurityContext context) {
        this.context = context;
    }

    @Override
    public boolean isUserInRole(String role, SecurityProviderContext providerContext) {
        return this.context.isUserInRole(role);
    }

    @Override
    public boolean isAuthenticated(SecurityProviderContext providerContext) {
        return context.getUserPrincipal() != null;
    }
}
