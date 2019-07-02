package io.crnk.servlet.internal;

import io.crnk.core.engine.http.HttpRequestContextProvider;
import io.crnk.core.engine.result.ImmediateResult;
import io.crnk.core.engine.result.Result;
import io.crnk.core.engine.security.SecurityProvider;

public class ServletSecurityProvider implements SecurityProvider {

    private HttpRequestContextProvider contextProvider;

    public ServletSecurityProvider(HttpRequestContextProvider contextProvider) {
        this.contextProvider = contextProvider;
    }

    @Override
    public Result<Boolean> isUserInRole(String role) {
        ServletRequestContext request = contextProvider.getRequestContext().unwrap
                (ServletRequestContext.class);
        return new ImmediateResult<>(request.getServletRequest().isUserInRole(role));
    }

	@Override
	public boolean isAuthenticated() {
		ServletRequestContext request = contextProvider.getRequestContext().unwrap
				(ServletRequestContext.class);
		return request.getServletRequest().getUserPrincipal() != null;
	}

}
