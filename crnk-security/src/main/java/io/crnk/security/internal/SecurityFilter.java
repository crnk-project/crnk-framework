package io.crnk.security.internal;

import io.crnk.core.engine.filter.FilterBehavior;
import io.crnk.core.engine.filter.RepositoryFilterBase;
import io.crnk.core.engine.filter.RepositoryFilterContext;
import io.crnk.core.engine.filter.RepositoryMetaFilterChain;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.resource.meta.MetaInformation;
import io.crnk.security.ResourcePermission;
import io.crnk.security.ResourcePermissionInformation;
import io.crnk.security.SecurityModule;

public class SecurityFilter extends RepositoryFilterBase {

	private SecurityModule module;

	public SecurityFilter(SecurityModule module) {
		this.module = module;
	}

	@Override
	public FilterBehavior filterResource(ResourceInformation resourceInformation, HttpMethod method) {
		ResourcePermission requiredPermission = ResourcePermission.fromMethod(method);
		boolean allowed = module.isAllowed(resourceInformation.getResourceType(), requiredPermission);
		return allowed ? FilterBehavior.NONE : FilterBehavior.FORBIDDEN;
	}

	@Override
	public <T> MetaInformation filterMeta(RepositoryFilterContext context, Iterable<T> resources,
										  RepositoryMetaFilterChain chain) {
		MetaInformation metaInformation = chain.doFilter(context, resources);
		if (metaInformation instanceof ResourcePermissionInformation) {
			ResourcePermissionInformation permissionInformation = (ResourcePermissionInformation) metaInformation;

			QueryAdapter queryAdapter = context.getRequest().getQueryAdapter();
			Class<?> resourceClass = queryAdapter.getResourceInformation().getResourceClass();
			permissionInformation.setResourcePermission(module.getResourcePermission(resourceClass));
		}
		return metaInformation;
	}

}
