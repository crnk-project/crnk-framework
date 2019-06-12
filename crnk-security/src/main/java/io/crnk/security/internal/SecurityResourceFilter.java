package io.crnk.security.internal;

import io.crnk.core.engine.filter.FilterBehavior;
import io.crnk.core.engine.filter.ResourceFilterBase;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.module.Module;
import io.crnk.security.ResourcePermission;
import io.crnk.security.SecurityModule;

public class SecurityResourceFilter extends ResourceFilterBase {

	private final Module.ModuleContext context;

	private SecurityModule module;

	public SecurityResourceFilter(SecurityModule module, Module.ModuleContext context) {
		this.module = module;
		this.context = context;
	}

	@Override
	public FilterBehavior filterResource(ResourceInformation resourceInformation, HttpMethod method) {
		ResourcePermission requiredPermission = ResourcePermission.fromMethod(method);
		boolean allowed = module.isAllowed(resourceInformation.getResourceType(), requiredPermission);
		boolean authenticated = context.getSecurityProvider().isAuthenticated();
		return allowed ? FilterBehavior.NONE : authenticated ? FilterBehavior.FORBIDDEN : FilterBehavior.UNAUTHORIZED;
	}
}
