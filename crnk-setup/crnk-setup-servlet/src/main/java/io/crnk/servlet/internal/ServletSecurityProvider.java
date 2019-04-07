package io.crnk.servlet.internal;

import io.crnk.core.engine.http.HttpRequestContextProvider;
import io.crnk.core.engine.security.SecurityProvider;

public class ServletSecurityProvider implements SecurityProvider {

    private HttpRequestContextProvider contextProvider;

    public ServletSecurityProvider(HttpRequestContextProvider contextProvider) {
        this.contextProvider = contextProvider;
    }

    @Override
    public boolean isUserInRole(String role) {
        ServletRequestContext request = contextProvider.getRequestContext().unwrap
                (ServletRequestContext.class);
        return request.getServletRequest().isUserInRole(role);
    }

}
