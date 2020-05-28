package io.crnk.security.internal;

import io.crnk.core.engine.filter.FilterBehavior;
import io.crnk.core.engine.filter.ResourceFilterBase;
import io.crnk.core.engine.filter.ResourceFilterContext;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.query.QueryContext;
import io.crnk.core.module.Module;
import io.crnk.security.ResourcePermission;
import io.crnk.security.SecurityModule;

public class SecurityResourceFilter extends ResourceFilterBase {

    private final Module.ModuleContext moduleContext;

    private SecurityModule module;

    public SecurityResourceFilter(SecurityModule module, Module.ModuleContext context) {
        this.module = module;
        this.moduleContext = context;
    }

    @Override
    public FilterBehavior filterResource(ResourceFilterContext context, ResourceInformation resourceInformation, HttpMethod method) {
        QueryContext queryContext = context.getQueryContext();
        ResourcePermission requiredPermission = ResourcePermission.fromMethod(method);
        boolean allowed = module.isAllowed(queryContext, resourceInformation.getResourceType(), requiredPermission);
        boolean authenticated = moduleContext.getSecurityProvider().isAuthenticated(() -> context.getQueryContext());
        return allowed ? FilterBehavior.NONE : authenticated ? FilterBehavior.FORBIDDEN : FilterBehavior.UNAUTHORIZED;
    }
}
