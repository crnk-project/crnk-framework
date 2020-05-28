package io.crnk.core.engine.version;

import io.crnk.core.engine.filter.FilterBehavior;
import io.crnk.core.engine.filter.ResourceFilter;
import io.crnk.core.engine.filter.ResourceFilterContext;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.information.resource.VersionRange;
import io.crnk.core.engine.query.QueryContext;
import io.crnk.core.module.Module;
import io.crnk.core.resource.annotations.JsonApiVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Enables support for {@link JsonApiVersion}.
 */
public class VersionModule implements Module {

    private static final Logger LOGGER = LoggerFactory.getLogger(VersionModule.class);

    @Override
    public String getModuleName() {
        return "crnk.version";
    }

    @Override
    public void setupModule(ModuleContext context) {
        context.addResourceFilter(new VersionResourceFilter());
    }

    class VersionResourceFilter implements ResourceFilter {

        @Override
        public FilterBehavior filterResource(ResourceFilterContext filterContext, ResourceInformation resourceInformation, HttpMethod method) {
            QueryContext queryContext = filterContext.getQueryContext();
            VersionRange versionRange = resourceInformation.getVersionRange();
            FilterBehavior filterBehavior = versionRange.contains(queryContext.getRequestVersion()) ? FilterBehavior.NONE : FilterBehavior.FORBIDDEN;
            LOGGER.debug("{} for {}", filterBehavior, resourceInformation);
            return filterBehavior;
        }

        @Override
        public FilterBehavior filterField(ResourceFilterContext filterContext, ResourceField field, HttpMethod method) {
            QueryContext queryContext = filterContext.getQueryContext();
            VersionRange versionRange = field.getVersionRange();
            FilterBehavior filterBehavior = versionRange.contains(queryContext.getRequestVersion()) ? FilterBehavior.NONE : FilterBehavior.FORBIDDEN;
            LOGGER.debug("{} for {}", filterBehavior, field);
            return filterBehavior;
        }
    }
}
