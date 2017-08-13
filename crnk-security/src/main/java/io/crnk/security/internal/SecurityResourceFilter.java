package io.crnk.security.internal;

import io.crnk.core.engine.filter.FilterBehavior;
import io.crnk.core.engine.filter.ResourceFilterBase;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.security.ResourcePermission;
import io.crnk.security.SecurityModule;

public class SecurityResourceFilter extends ResourceFilterBase {

	private SecurityModule module;

	public SecurityResourceFilter(SecurityModule module) {
		this.module = module;
	}

	@Override
	public FilterBehavior filterResource(ResourceInformation resourceInformation, HttpMethod method) {
		ResourcePermission requiredPermission = ResourcePermission.fromMethod(method);
		boolean allowed = module.isAllowed(resourceInformation.getResourceType(), requiredPermission);
		return allowed ? FilterBehavior.NONE : FilterBehavior.FORBIDDEN;
	}
}
