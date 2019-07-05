package io.crnk.rs;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.information.repository.ResourceRepositoryInformation;
import io.crnk.core.engine.internal.repository.ResourceRepositoryAdapter;
import io.crnk.core.engine.internal.utils.UrlUtils;
import io.crnk.core.engine.properties.PropertiesProvider;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.engine.url.ServiceUrlProvider;
import io.crnk.core.module.Module;
import io.crnk.core.queryspec.mapper.QuerySpecUrlMapper;
import io.crnk.rs.internal.JaxrsModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.SecurityContext;
import java.util.Collection;

/**
 * Basic Crnk feature that initializes core classes and provides a starting point to use the framework in
 * another projects.
 */
@ConstrainedTo(RuntimeType.SERVER)
public class CrnkFeature implements Feature {

    private static final Logger LOGGER = LoggerFactory.getLogger(CrnkFeature.class);

    private CrnkBoot boot = new CrnkBoot();

    @Context
    protected SecurityContext securityContext;

    private boolean securityEnabled = true;

    public CrnkFeature() {
        // nothing to do
    }

    /**
     * Sets a custom ServiceUrlProvider.
     */
    public void setServiceUrlProvider(ServiceUrlProvider serviceUrlProvider) {
        boot.setServiceUrlProvider(serviceUrlProvider);
    }

    public void addModule(Module module) {
        boot.addModule(module);
    }

    @Override
    public boolean configure(final FeatureContext context) {
        LOGGER.debug("configuring CrnkFeature");
        boot.setPropertiesProvider(createPropertiesProvider(context));
        boot.getCoreModule()
                .setDefaultRepositoryInformationProvider(new JaxrsModule.JaxrsResourceRepositoryInformationProvider());
        boot.addModule(new JaxrsModule(securityEnabled ? securityContext : null));

        boot.boot();

        CrnkFilter crnkFilter = createCrnkFilter();
        context.register(crnkFilter);

        registerActionRepositories(context, boot);
        LOGGER.debug("configured CrnkFeature");
        return true;
    }

    protected PropertiesProvider createPropertiesProvider(FeatureContext context) {
        return key -> (String) context.getConfiguration().getProperty(key);
    }

    public void setSecurityEnabled(boolean securityEnabled) {
        this.securityEnabled = securityEnabled;
    }

    /**
     * All repositories with JAX-RS action need to be registered with JAX-RS as singletons.
     *
     * @param context of jaxrs
     * @param boot    of crnk
     */
    private void registerActionRepositories(FeatureContext context, CrnkBoot boot) {
        ResourceRegistry resourceRegistry = boot.getResourceRegistry();
        Collection<RegistryEntry> registryEntries = resourceRegistry.getEntries();
        for (RegistryEntry registryEntry : registryEntries) {
            ResourceRepositoryInformation repositoryInformation = registryEntry.getRepositoryInformation();
            if (repositoryInformation != null && !repositoryInformation.getActions().isEmpty()) {
                ResourceRepositoryAdapter repositoryAdapter = registryEntry.getResourceRepository();
                Object resourceRepository = repositoryAdapter.getImplementation();
                context.register(resourceRepository);
            }
        }
    }

    protected CrnkFilter createCrnkFilter() {
        return new CrnkFilter(this);
    }

    public ObjectMapper getObjectMapper() {
        return boot.getObjectMapper();
    }

    public void setDefaultPageLimit(Long defaultPageLimit) {
        boot.setDefaultPageLimit(defaultPageLimit);
    }

    public QuerySpecUrlMapper getUrlMapper() {
        return boot.getUrlMapper();
    }

    public CrnkBoot getBoot() {
        return boot;
    }

    public String getWebPathPrefix() {
        return UrlUtils.removeLeadingSlash(boot.getWebPathPrefix());
    }

}
