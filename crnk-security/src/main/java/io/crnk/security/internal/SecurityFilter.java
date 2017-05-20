package io.crnk.security.internal;

import io.crnk.core.engine.dispatcher.RepositoryRequestSpec;
import io.crnk.core.engine.filter.RepositoryFilterBase;
import io.crnk.core.engine.filter.RepositoryFilterContext;
import io.crnk.core.engine.filter.RepositoryMetaFilterChain;
import io.crnk.core.engine.filter.RepositoryRequestFilterChain;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.exception.ForbiddenException;
import io.crnk.core.repository.response.JsonApiResponse;
import io.crnk.core.resource.meta.MetaInformation;
import io.crnk.security.ResourcePermission;
import io.crnk.security.ResourcePermissionInformation;
import io.crnk.security.SecurityModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SecurityFilter extends RepositoryFilterBase {

	private static final Logger LOGGER = LoggerFactory.getLogger(SecurityFilter.class);

	private SecurityModule module;

	public SecurityFilter(SecurityModule module) {
		this.module = module;
	}

	@Override
	public JsonApiResponse filterRequest(RepositoryFilterContext context, RepositoryRequestFilterChain chain) {
		RepositoryRequestSpec request = context.getRequest();
		QueryAdapter queryAdapter = request.getQueryAdapter();
		Class<?> resourceClass = queryAdapter.getResourceInformation().getResourceClass();

		HttpMethod method = request.getMethod();
		ResourcePermission requiredPermission = ResourcePermission.fromMethod(method);

		boolean allowed = module.isAllowed(resourceClass, requiredPermission);
		if (!allowed) {
			String msg = "user not allowed to access " + resourceClass.getName();
			throw new ForbiddenException(msg);
		} else {
			LOGGER.debug("user allowed to access {}", resourceClass.getSimpleName());
			return chain.doFilter(context);
		}
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
