package io.crnk.servlet.internal;

import io.crnk.core.engine.http.HttpRequestContextProvider;
import io.crnk.core.engine.security.SecurityProvider;
import io.crnk.core.engine.security.SecurityProviderContext;

public class ServletSecurityProvider implements SecurityProvider {

    private HttpRequestContextProvider contextProvider;

    public ServletSecurityProvider(HttpRequestContextProvider contextProvider) {
        this.contextProvider = contextProvider;
    }

    @Override
    public boolean isUserInRole(String role, SecurityProviderContext context) {
        ServletRequestContext request = contextProvider.getRequestContext().unwrap
                (ServletRequestContext.class);
        return request.getServletRequest().isUserInRole(role);
    }

    @Override
    public boolean isAuthenticated(SecurityProviderContext context) {
        ServletRequestContext request = contextProvider.getRequestContext().unwrap
                (ServletRequestContext.class);
        return request.getServletRequest().getUserPrincipal() != null;
    }

}
