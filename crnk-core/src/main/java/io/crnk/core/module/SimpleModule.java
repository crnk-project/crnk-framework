package io.crnk.core.module;

import io.crnk.core.engine.error.ExceptionMapper;
import io.crnk.core.engine.filter.DocumentFilter;
import io.crnk.core.engine.filter.RepositoryFilter;
import io.crnk.core.engine.filter.ResourceFilter;
import io.crnk.core.engine.filter.ResourceModificationFilter;
import io.crnk.core.engine.http.HttpRequestProcessor;
import io.crnk.core.engine.http.HttpStatusBehavior;
import io.crnk.core.engine.information.NamingStrategy;
import io.crnk.core.engine.information.contributor.ResourceFieldContributor;
import io.crnk.core.engine.information.repository.RepositoryInformationProvider;
import io.crnk.core.engine.information.resource.ResourceInformationProvider;
import io.crnk.core.engine.internal.exception.ExceptionMapperLookup;
import io.crnk.core.engine.internal.repository.RepositoryAdapterFactory;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistryPart;
import io.crnk.core.engine.security.SecurityProvider;
import io.crnk.core.module.discovery.ResourceLookup;
import io.crnk.core.queryspec.pagingspec.PagingBehavior;
import io.crnk.core.repository.decorate.RepositoryDecoratorFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Vanilla {@link Module} implementation that allows registration of extensions.
 */
public class SimpleModule implements Module {

    private List<ResourceInformationProvider> resourceInformationProviders = new ArrayList<>();

    private List<HttpRequestProcessor> httpRequestProcessors = new ArrayList<>();

    private List<RepositoryInformationProvider> repositoryInformationProviders = new ArrayList<>();

    private List<DocumentFilter> documentFilters = new ArrayList<>();

    private List<RepositoryFilter> repositoryFilters = new ArrayList<>();

    private List<ResourceFilter> resourceFilters = new ArrayList<>();

    private List<HttpStatusBehavior> httpStatusBehaviors = new ArrayList<>();

    private List<ResourceModificationFilter> resourceModificationFilters = new ArrayList<>();

    private List<ResourceFieldContributor> resourceFieldContributors = new ArrayList<>();

    private List<RepositoryDecoratorFactory> repositoryDecoratorFactories = new ArrayList<>();

    private List<SecurityProvider> securityProviders = new ArrayList<>();

    private List<ResourceLookup> resourceLookups = new ArrayList<>();

    private List<PagingBehavior> pagingBehaviors = new ArrayList<>();

    private List<com.fasterxml.jackson.databind.Module> jacksonModules = new ArrayList<>();

    private List<Object> repositories = new ArrayList<>();

    private List<ExceptionMapperLookup> exceptionMapperLookups = new ArrayList<>();

    private List<RegistryEntry> registryEntries = new ArrayList<>();

    private List<ModuleExtension> extensions = new ArrayList<>();

    private List<RepositoryAdapterFactory> repositoryAdapterFactories = new ArrayList<>();

    private List<NamingStrategy> namingStrategies = new ArrayList<>();

    private String moduleName;

    private ModuleContext context;

    private Map<String, ResourceRegistryPart> registryParts = new HashMap<>();

    public SimpleModule(String moduleName) {
        this.moduleName = moduleName;
    }

    @Override
    public String getModuleName() {
        return moduleName;
    }

    @Override
    public void setupModule(ModuleContext context) {
        this.context = context;
        for (ResourceInformationProvider resourceInformationProvider : resourceInformationProviders) {
            context.addResourceInformationProvider(resourceInformationProvider);
        }
        for (RepositoryInformationProvider resourceInformationBuilder : repositoryInformationProviders) {
            context.addRepositoryInformationBuilder(resourceInformationBuilder);
        }
        for (ResourceLookup resourceLookup : resourceLookups) {
            context.addResourceLookup(resourceLookup);
        }
        for (DocumentFilter filter : documentFilters) {
            context.addFilter(filter);
        }
        for (RepositoryFilter filter : repositoryFilters) {
            context.addRepositoryFilter(filter);
        }
        for (ResourceFilter filter : resourceFilters) {
            context.addResourceFilter(filter);
        }
        for (HttpStatusBehavior httpStatusBehavior : httpStatusBehaviors) {
            context.addHttpStatusBehavior(httpStatusBehavior);
        }
        for (ResourceModificationFilter filter : resourceModificationFilters) {
            context.addResourceModificationFilter(filter);
        }
        for (RepositoryDecoratorFactory decorator : repositoryDecoratorFactories) {
            context.addRepositoryDecoratorFactory(decorator);
        }
        for (com.fasterxml.jackson.databind.Module jacksonModule : jacksonModules) {
            context.addJacksonModule(jacksonModule);
        }
        for (RepositoryAdapterFactory factory : repositoryAdapterFactories) {
            context.addRepositoryAdapterFactory(factory);
        }
        for (NamingStrategy namingStrategy : namingStrategies) {
            context.addNamingStrategy(namingStrategy);
        }
        for (Object repository : repositories) {
            context.addRepository(repository);
        }
        for (ExceptionMapperLookup exceptionMapperLookup : exceptionMapperLookups) {
            context.addExceptionMapperLookup(exceptionMapperLookup);
        }
        for (ResourceFieldContributor resourceFieldContributor : resourceFieldContributors) {
            context.addResourceFieldContributor(resourceFieldContributor);
        }
        for (HttpRequestProcessor httpRequestProcessor : httpRequestProcessors) {
            context.addHttpRequestProcessor(httpRequestProcessor);
        }
        for (ModuleExtension extension : extensions) {
            context.addExtension(extension);
        }
        for (PagingBehavior pagingBehavior : pagingBehaviors) {
            context.addPagingBehavior(pagingBehavior);
        }
        for (SecurityProvider securityProvider : securityProviders) {
            context.addSecurityProvider(securityProvider);
        }
    }

    private void checkInitialized() {
        if (context != null) {
            throw new IllegalStateException("module cannot be changed addModule was called");
        }
    }

    /**
     * Registers a new {@link ResourceInformationProvider} with this module.
     *
     * @param resourceInformationProvider resource information builder
     */
    public void addResourceInformationProvider(ResourceInformationProvider resourceInformationProvider) {
        checkInitialized();
        resourceInformationProviders.add(resourceInformationProvider);
    }

    /**
     * Registers a new {@link RepositoryInformationProvider} with this module.
     *
     * @param repositoryInformationProvider repository information builder
     */
    public void addRepositoryInformationBuilder(RepositoryInformationProvider repositoryInformationProvider) {
        checkInitialized();
        repositoryInformationProviders.add(repositoryInformationProvider);
    }

    public void addExceptionMapperLookup(ExceptionMapperLookup exceptionMapperLookup) {
        checkInitialized();
        exceptionMapperLookups.add(exceptionMapperLookup);
    }

    public void addExceptionMapper(@SuppressWarnings("rawtypes") ExceptionMapper exceptionMapper) {
        checkInitialized();
        ExceptionMapperLookup exceptionMapperLookup = new CollectionExceptionMapperLookup(exceptionMapper);
        exceptionMapperLookups.add(exceptionMapperLookup);
    }

    protected List<ResourceInformationProvider> getResourceInformationProviders() {
        checkInitialized();
        return Collections.unmodifiableList(resourceInformationProviders);
    }

    protected List<RepositoryInformationProvider> getRepositoryInformationProviders() {
        checkInitialized();
        return Collections.unmodifiableList(repositoryInformationProviders);
    }

    public Map<String, ResourceRegistryPart> getRegistryParts() {
        return Collections.unmodifiableMap(registryParts);
    }

    public void addExtension(ModuleExtension extension) {
        checkInitialized();
        extensions.add(extension);
    }

    public void addFilter(DocumentFilter filter) {
        checkInitialized();
        documentFilters.add(filter);
    }

    public void addRepositoryFilter(RepositoryFilter filter) {
        checkInitialized();
        repositoryFilters.add(filter);
    }

    public void addResourceFilter(ResourceFilter filter) {
        checkInitialized();
        resourceFilters.add(filter);
    }


    public void addHttpStatusBehavior(HttpStatusBehavior httpStatusBehavior) {
        checkInitialized();
        httpStatusBehaviors.add(httpStatusBehavior);
    }

    public void addResourceFieldContributor(ResourceFieldContributor resourceFieldContributor) {
        checkInitialized();
        resourceFieldContributors.add(resourceFieldContributor);
    }

    protected List<ResourceFieldContributor> getResourceFieldContributors() {
        checkInitialized();
        return Collections.unmodifiableList(resourceFieldContributors);
    }

    public void addResourceModificationFilter(ResourceModificationFilter filter) {
        checkInitialized();
        resourceModificationFilters.add(filter);
    }

    public void addRepositoryDecoratorFactory(RepositoryDecoratorFactory decorator) {
        checkInitialized();
        repositoryDecoratorFactories.add(decorator);
    }

    protected List<DocumentFilter> getFilters() {
        checkInitialized();
        return Collections.unmodifiableList(documentFilters);
    }

    protected List<RepositoryFilter> getRepositoryFilters() {
        checkInitialized();
        return Collections.unmodifiableList(repositoryFilters);
    }

    protected List<ResourceFilter> getResourceFilters() {
        checkInitialized();
        return Collections.unmodifiableList(resourceFilters);
    }

    protected List<ResourceModificationFilter> getResourceModificationFilters() {
        checkInitialized();
        return Collections.unmodifiableList(resourceModificationFilters);
    }

    protected List<ModuleExtension> getExtensions() {
        checkInitialized();
        return Collections.unmodifiableList(extensions);
    }

    protected List<RepositoryDecoratorFactory> getRepositoryDecoratorFactories() {
        checkInitialized();
        return Collections.unmodifiableList(repositoryDecoratorFactories);
    }

    public void addSecurityProvider(SecurityProvider securityProvider) {
        checkInitialized();
        securityProviders.add(securityProvider);
    }

    public void addJacksonModule(com.fasterxml.jackson.databind.Module module) {
        checkInitialized();
        jacksonModules.add(module);
    }

    protected List<com.fasterxml.jackson.databind.Module> getJacksonModules() {
        checkInitialized();
        return Collections.unmodifiableList(jacksonModules);
    }

    /**
     * Add the given {@link PagingBehavior} to the module
     *
     * @param pagingBehavior the paging behavior
     */
    public void addPagingBehavior(PagingBehavior pagingBehavior) {
        checkInitialized();

        // avoid adding the same type(!) of behavior twice, error out if that's the case
        boolean behaviorTypeAdded = pagingBehaviors
                .stream()
                .anyMatch(pbh -> pbh.getClass().equals(pagingBehavior.getClass()));

        if (!behaviorTypeAdded) {
            pagingBehaviors.add(pagingBehavior);
        } else {
            throw new IllegalArgumentException(
                    "PagingBehavior of same type already added. Type:"
                            + pagingBehavior.getClass().getSimpleName());
        }
    }

    public List<PagingBehavior> getPagingBehaviors() {
        checkInitialized();
        return Collections.unmodifiableList(pagingBehaviors);
    }

    /**
     * Registers a new {@link ResourceLookup} with this module.
     *
     * @param resourceLookup resource lookup
     */
    public void addResourceLookup(ResourceLookup resourceLookup) {
        checkInitialized();
        resourceLookups.add(resourceLookup);
    }

    protected List<ResourceLookup> getResourceLookups() {
        checkInitialized();
        return Collections.unmodifiableList(resourceLookups);
    }

    public void addRepository(Object repository) {
        checkInitialized();
        repositories.add(repository);
    }

    public List<Object> getRepositories() {
        return Collections.unmodifiableList(repositories);
    }

    public List<ExceptionMapperLookup> getExceptionMapperLookups() {
        return Collections.unmodifiableList(exceptionMapperLookups);
    }

    public List<SecurityProvider> getSecurityProviders() {
        return Collections.unmodifiableList(securityProviders);
    }

    public List<HttpStatusBehavior> getHttpStatusBehaviors() {
        return Collections.unmodifiableList(httpStatusBehaviors);
    }

    public void addHttpRequestProcessor(HttpRequestProcessor httpRequestProcessor) {
        httpRequestProcessors.add(httpRequestProcessor);
    }

    public List<HttpRequestProcessor> getHttpRequestProcessors() {
        return Collections.unmodifiableList(httpRequestProcessors);
    }

    public void addRepositoryAdapterFactory(RepositoryAdapterFactory repositoryAdapterFactory) {
        repositoryAdapterFactories.add(repositoryAdapterFactory);
    }

    public List<RepositoryAdapterFactory> getRepositoryAdapterFactories() {
        return Collections.unmodifiableList(repositoryAdapterFactories);
    }

    public void addNamingStrategy(NamingStrategy namingStrategy) {
        namingStrategies.add(namingStrategy);
    }

    public List<NamingStrategy> getNamingStrategies() {
        return Collections.unmodifiableList(namingStrategies);
    }

    public void addRegistryPart(String prefix, ResourceRegistryPart part) {
        if (registryParts.containsKey(prefix)) {
            throw new IllegalStateException("part with prefix " + prefix + " is already registered");
        }
        registryParts.put(prefix, part);
    }

    public List<RegistryEntry> getRegistryEntries() {
        return Collections.unmodifiableList(registryEntries);
    }

    public void addRegistryEntry(RegistryEntry entry) {
        registryEntries.add(entry);
    }

    @SuppressWarnings("rawtypes")
    private static class CollectionExceptionMapperLookup implements ExceptionMapperLookup {

        private List<ExceptionMapper> set;

        private CollectionExceptionMapperLookup(List<ExceptionMapper> set) {
            this.set = set;
        }

        public CollectionExceptionMapperLookup(ExceptionMapper exceptionMapper) {
            this(Arrays.asList(exceptionMapper));
        }

        @Override
        public List<ExceptionMapper> getExceptionMappers() {
            return set;
        }
    }
}
