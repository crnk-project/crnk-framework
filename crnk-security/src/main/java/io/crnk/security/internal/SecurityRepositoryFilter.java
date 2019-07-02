package io.crnk.security.internal;

import io.crnk.core.engine.filter.RepositoryFilterBase;
import io.crnk.core.engine.filter.RepositoryFilterContext;
import io.crnk.core.engine.filter.RepositoryMetaFilterChain;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.query.QueryContext;
import io.crnk.core.resource.meta.MetaInformation;
import io.crnk.security.ResourcePermissionInformation;
import io.crnk.security.SecurityModule;

import java.util.Collection;

public class SecurityRepositoryFilter extends RepositoryFilterBase {

    private SecurityModule module;

    public SecurityRepositoryFilter(SecurityModule module) {
        this.module = module;
    }

    @Override
    public <T> MetaInformation filterMeta(RepositoryFilterContext context, Collection<T> resources,
                                          RepositoryMetaFilterChain chain) {
        MetaInformation metaInformation = chain.doFilter(context, resources);
        if (metaInformation instanceof ResourcePermissionInformation) {
            ResourcePermissionInformation permissionInformation = (ResourcePermissionInformation) metaInformation;

            QueryAdapter queryAdapter = context.getRequest().getQueryAdapter();
            QueryContext queryContext = queryAdapter.getQueryContext();
            Class<?> resourceClass = queryAdapter.getResourceInformation().getResourceClass();
            permissionInformation.setResourcePermission(module.getResourcePermission(queryContext, resourceClass));
        }
        return metaInformation;
    }

}
