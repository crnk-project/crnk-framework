package io.crnk.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.crnk.client.action.ActionStubFactory;
import io.crnk.client.action.ActionStubFactoryContext;
import io.crnk.client.http.HttpAdapter;
import io.crnk.client.http.HttpAdapterProvider;
import io.crnk.client.http.apache.HttpClientAdapterProvider;
import io.crnk.client.http.okhttp.OkHttpAdapterProvider;
import io.crnk.client.internal.ClientDocumentMapper;
import io.crnk.client.internal.ClientStubInvocationHandler;
import io.crnk.client.internal.RelationshipRepositoryStubImpl;
import io.crnk.client.internal.ResourceRepositoryStubImpl;
import io.crnk.client.internal.proxy.BasicProxyFactory;
import io.crnk.client.internal.proxy.ClientProxyFactory;
import io.crnk.client.internal.proxy.ClientProxyFactoryContext;
import io.crnk.client.module.ClientModule;
import io.crnk.client.module.ClientModuleFactory;
import io.crnk.client.module.HttpAdapterAware;
import io.crnk.core.boot.CrnkProperties;
import io.crnk.core.engine.document.Resource;
import io.crnk.core.engine.information.repository.RepositoryInformationProvider;
import io.crnk.core.engine.information.repository.RepositoryMethodAccess;
import io.crnk.core.engine.information.repository.ResourceRepositoryInformation;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.information.resource.ResourceInformationProvider;
import io.crnk.core.engine.information.resource.ResourceInformationProviderModule;
import io.crnk.core.engine.internal.exception.ExceptionMapperLookup;
import io.crnk.core.engine.internal.exception.ExceptionMapperRegistry;
import io.crnk.core.engine.internal.exception.ExceptionMapperRegistryBuilder;
import io.crnk.core.engine.internal.information.repository.ResourceRepositoryInformationImpl;
import io.crnk.core.engine.internal.jackson.JacksonModule;
import io.crnk.core.engine.internal.registry.DefaultRegistryEntryBuilder;
import io.crnk.core.engine.internal.registry.LegacyRegistryEntry;
import io.crnk.core.engine.internal.registry.ResourceRegistryImpl;
import io.crnk.core.engine.internal.repository.RelationshipRepositoryAdapter;
import io.crnk.core.engine.internal.repository.RelationshipRepositoryAdapterImpl;
import io.crnk.core.engine.internal.repository.ResourceRepositoryAdapter;
import io.crnk.core.engine.internal.utils.ClassUtils;
import io.crnk.core.engine.internal.utils.JsonApiUrlBuilder;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.engine.internal.utils.UrlUtils;
import io.crnk.core.engine.properties.NullPropertiesProvider;
import io.crnk.core.engine.properties.PropertiesProvider;
import io.crnk.core.engine.properties.SystemPropertiesProvider;
import io.crnk.core.engine.query.QueryContext;
import io.crnk.core.engine.registry.DefaultResourceRegistryPart;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.engine.registry.ResponseRelationshipEntry;
import io.crnk.core.engine.url.ConstantServiceUrlProvider;
import io.crnk.core.engine.url.ServiceUrlProvider;
import io.crnk.core.exception.InvalidResourceException;
import io.crnk.core.module.Module;
import io.crnk.core.module.ModuleRegistry;
import io.crnk.core.module.discovery.DefaultServiceDiscoveryFactory;
import io.crnk.core.module.discovery.FallbackServiceDiscoveryFactory;
import io.crnk.core.module.discovery.ResourceLookup;
import io.crnk.core.module.discovery.ServiceDiscovery;
import io.crnk.core.module.discovery.ServiceDiscoveryFactory;
import io.crnk.core.module.internal.DefaultRepositoryInformationProviderContext;
import io.crnk.core.queryspec.mapper.DefaultQuerySpecUrlMapper;
import io.crnk.core.queryspec.mapper.QuerySpecUrlMapper;
import io.crnk.core.queryspec.pagingspec.LimitBoundedPagingBehavior;
import io.crnk.core.queryspec.pagingspec.OffsetLimitPagingBehavior;
import io.crnk.core.queryspec.pagingspec.PagingBehavior;
import io.crnk.core.queryspec.pagingspec.PagingSpec;
import io.crnk.core.repository.ManyRelationshipRepository;
import io.crnk.core.repository.OneRelationshipRepository;
import io.crnk.core.repository.RelationshipRepository;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.core.resource.list.DefaultResourceList;
import io.crnk.legacy.internal.DirectResponseRelationshipEntry;
import io.crnk.legacy.internal.DirectResponseResourceEntry;
import io.crnk.legacy.locator.JsonServiceLocator;
import io.crnk.legacy.locator.SampleJsonServiceLocator;
import io.crnk.legacy.registry.RepositoryInstanceBuilder;
import io.crnk.legacy.repository.LegacyRelationshipRepository;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;

/**
 * Client implementation giving access to JSON API repositories using stubs.
 */
public class CrnkClient {

    private static final String REST_TEMPLATE_PROVIDER_NAME = "io.crnk.spring.client.RestTemplateAdapterProvider";

    private HttpAdapter httpAdapter;

    private ObjectMapper objectMapper;

    private ResourceRegistry resourceRegistry;

    private ModuleRegistry moduleRegistry;

    private JsonApiUrlBuilder urlBuilder;

    private boolean initialized = false;

    private ExceptionMapperRegistry exceptionMapperRegistry;

    private ActionStubFactory actionStubFactory;

    private ClientDocumentMapper documentMapper;

    private ServiceDiscovery serviceDiscovery;

    private ServiceDiscoveryFactory serviceDiscoveryFactory = new DefaultServiceDiscoveryFactory();

    private JsonServiceLocator serviceLocator = new SampleJsonServiceLocator();

    private PropertiesProvider propertiesProvider = new SystemPropertiesProvider();

    private Long defaultPageLimit = null;

    private Long maxPageLimit = null;

    private QueryContext queryContext = new QueryContext();

    private List<HttpAdapterProvider> httpAdapterProviders = new ArrayList<>();

    private ClientFormat format = ClientFormat.JSONAPI;

    public enum ClientType {
        SIMPLE_lINKS,
        OBJECT_LINKS
    }

    public CrnkClient(String serviceUrl) {
        this(new ConstantServiceUrlProvider(UrlUtils.removeTrailingSlash(serviceUrl)), ClientType.SIMPLE_lINKS);
    }

    public CrnkClient(String serviceUrl, ClientType clientType) {
        this(new ConstantServiceUrlProvider(UrlUtils.removeTrailingSlash(serviceUrl)), clientType);
    }

    public CrnkClient(ServiceUrlProvider serviceUrlProvider, ClientType clientType) {
        if (ClassUtils.existsClass(REST_TEMPLATE_PROVIDER_NAME)) {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            Class providerClass = ClassUtils.loadClass(classLoader, REST_TEMPLATE_PROVIDER_NAME);
            HttpAdapterProvider provider = (HttpAdapterProvider) ClassUtils.newInstance(providerClass);
            registerHttpAdapterProvider(provider);
        }
        this.registerHttpAdapterProvider(new OkHttpAdapterProvider());
        this.registerHttpAdapterProvider(new HttpClientAdapterProvider());

        moduleRegistry = new ModuleRegistry(false);
        moduleRegistry.setUrlMapper(new DefaultQuerySpecUrlMapper());
        moduleRegistry.getHttpRequestContextProvider().setServiceUrlProvider(serviceUrlProvider);
        moduleRegistry.addModule(new ClientModule());


        moduleRegistry.addModule(new ResourceInformationProviderModule());

        resourceRegistry = new ClientResourceRegistry(moduleRegistry);
        queryContext.setBaseUrl(serviceUrlProvider.getUrl());
        urlBuilder = new JsonApiUrlBuilder(moduleRegistry, queryContext);


        objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        switch (clientType) {
            case OBJECT_LINKS:
                initJacksonModule(true);
                documentMapper = new ClientDocumentMapper(moduleRegistry, objectMapper, new PropertiesProvider() {
                    @Override
                    public String getProperty(String key) {
                        if (key.equals(CrnkProperties.SERIALIZE_LINKS_AS_OBJECTS)) {
                            return "true";
                        }
                        return null;
                    }
                });
                break;
            default:
                initJacksonModule(false);
                documentMapper = new ClientDocumentMapper(moduleRegistry, objectMapper, new NullPropertiesProvider());
        }

        setProxyFactory(new BasicProxyFactory());
    }

    public ClientFormat getFormat() {
        return format;
    }

    public ServiceDiscovery getServiceDiscovery() {
        setupServiceDiscovery();
        return serviceDiscovery;
    }

    private void setupServiceDiscovery() {
        if (serviceDiscovery == null) {
            // revert to reflection-based approach if no ServiceDiscovery is
            // found
            FallbackServiceDiscoveryFactory fallback =
                    new FallbackServiceDiscoveryFactory(serviceDiscoveryFactory, serviceLocator, propertiesProvider);
            serviceDiscovery = fallback.getInstance();
            moduleRegistry.setServiceDiscovery(serviceDiscovery);
        }
    }

    public void setServiceDiscovery(ServiceDiscovery serviceDiscovery) {
        verifyNotInitialized();
        PreconditionUtil.verify(this.serviceDiscovery == null, "service discovery already set");
        this.serviceDiscovery = serviceDiscovery;
        moduleRegistry.setServiceDiscovery(serviceDiscovery);
    }

    private void verifyNotInitialized() {
        PreconditionUtil.verify(!initialized, "CrnkClient already initialized and can no longer be reconfigured");
    }

    public QueryContext getQueryContext() {
        return queryContext;
    }

    private void setupPagingBehavior() {
        if (moduleRegistry.getPagingBehaviors().isEmpty()) {
            moduleRegistry.addAllPagingBehaviors(serviceDiscovery.getInstancesByType(PagingBehavior.class));
        }

        if (moduleRegistry.getPagingBehaviors().isEmpty()) {
            moduleRegistry.addPagingBehavior(new OffsetLimitPagingBehavior());
        }

        for (PagingBehavior pagingBehavior : moduleRegistry.getPagingBehaviors()) {
            if (pagingBehavior instanceof LimitBoundedPagingBehavior) {
                if (defaultPageLimit != null) {
                    ((LimitBoundedPagingBehavior) pagingBehavior).setDefaultLimit(defaultPageLimit);
                }
                if (maxPageLimit != null) {
                    ((LimitBoundedPagingBehavior) pagingBehavior).setMaxPageLimit(maxPageLimit);
                }
            }
        }
    }

    private void initJacksonModule(final boolean serializeLinksAsObjects) {
        moduleRegistry.addModule(new JacksonModule(objectMapper, serializeLinksAsObjects));
    }

    /**
     * Finds and registers modules on the classpath trough the use of java.util.ServiceLoader.
     * Each module can register itself for lookup by registering a ClientModuleFactory.
     */
    public void findModules() {
        ServiceLoader<ClientModuleFactory> loader = ServiceLoader.load(ClientModuleFactory.class);

        Iterator<ClientModuleFactory> iterator = loader.iterator();
        while (iterator.hasNext()) {
            ClientModuleFactory factory = iterator.next();
            Module module = factory.create();
            addModule(module);
        }

        objectMapper.findAndRegisterModules();
    }

    public void setProxyFactory(ClientProxyFactory proxyFactory) {
        proxyFactory.init(new ClientProxyFactoryContext() {

            @Override
            public ModuleRegistry getModuleRegistry() {
                return moduleRegistry;
            }

            @Override
            public <T> DefaultResourceList<T> getCollection(Class<T> resourceClass, String url) {
                RegistryEntry entry = resourceRegistry.findEntry(resourceClass);
                ResourceInformation resourceInformation = entry.getResourceInformation();
                // TODO add decoration
                final ResourceRepositoryStubImpl<T, ?> repositoryStub =
                        new ResourceRepositoryStubImpl<>(CrnkClient.this, resourceClass, resourceInformation, urlBuilder);
                return repositoryStub.findAll(url);

            }
        });
        documentMapper.setProxyFactory(proxyFactory);
    }


    public void registerHttpAdapterProvider(HttpAdapterProvider httpAdapterProvider) {
        verifyNotInitialized();
        httpAdapterProviders.add(httpAdapterProvider);
    }

    public List<HttpAdapterProvider> getHttpAdapterProviders() {
        return httpAdapterProviders;
    }

    protected HttpAdapter detectHttpAdapter() {
        for (HttpAdapterProvider httpAdapterProvider : httpAdapterProviders) {
            if (httpAdapterProvider.isAvailable()) {
                return httpAdapterProvider.newInstance();
            }
        }
        throw new IllegalStateException(
                "no httpAdapter can be initialized, add okhttp3 (com.squareup.okhttp3:okhttp) or apache http client (org.apache"
                        + ".httpcomponents:httpclient) to the classpath");
    }

    protected void init() {
        if (initialized) {
            return;
        }
        initialized = true;

        setupServiceDiscovery();
        initHttpAdapter();

        setupPagingBehavior();
        initModuleRegistry();
        initExceptionMapperRegistry();
        initResources();

        Optional<Module> plainJsonModule = moduleRegistry.getModules().stream().filter(it -> it.getModuleName().equals("plain-json")).findFirst();
        if (plainJsonModule.isPresent()) {
            format = ClientFormat.PLAINJSON;
        }
    }

    private void initHttpAdapter() {
        if (httpAdapter == null) {
            httpAdapter = detectHttpAdapter();
        }
    }

    private void initResources() {
        ResourceLookup resourceLookup = moduleRegistry.getResourceLookup();
        for (Class<?> resourceClass : resourceLookup.getResourceClasses()) {
            getRepositoryForType(resourceClass);
        }
    }

    private void initModuleRegistry() {
        moduleRegistry.init(objectMapper);
    }

    private void initExceptionMapperRegistry() {
        ExceptionMapperLookup exceptionMapperLookup = moduleRegistry.getExceptionMapperLookup();
        exceptionMapperRegistry = new ExceptionMapperRegistryBuilder().build(exceptionMapperLookup);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private <T, I> RegistryEntry allocateRepository(Class<T> resourceClass, RegistryEntry parentEntry) {
        RegistryEntry entry = resourceRegistry.getEntry(resourceClass);
        if (entry != null) {
            return entry;
        }

        // allocate super types first
        ResourceInformationProvider resourceInformationProvider = moduleRegistry.getResourceInformationBuilder();
        Class<? super T> superclass = resourceClass.getSuperclass();
        if (parentEntry == null && superclass != null && superclass != Object.class && resourceInformationProvider.accept(superclass)) {
            parentEntry = allocateRepository(superclass, null);
        }

        entry = resourceRegistry.getEntry(resourceClass);
        if (entry != null) {
            return entry;
        }

        ResourceInformation resourceInformation = resourceInformationProvider.build(resourceClass);
        final ResourceRepository repositoryStub = (ResourceRepository)
                decorate(new ResourceRepositoryStubImpl<T, I>(this, resourceClass, resourceInformation, urlBuilder));

        // create interface for it!
        RepositoryInstanceBuilder repositoryInstanceBuilder = new RepositoryInstanceBuilder(null, null) {

            @Override
            public Object buildRepository() {
                return repositoryStub;
            }
        };
        ResourceRepositoryInformation repositoryInformation =
                new ResourceRepositoryInformationImpl(resourceInformation.getResourceType(),
                        resourceInformation, new HashMap<>(), RepositoryMethodAccess.ALL, true);
        ResourceEntry resourceEntry = new DirectResponseResourceEntry(repositoryInstanceBuilder, repositoryInformation);
        Map<ResourceField, ResponseRelationshipEntry> relationshipEntries = new HashMap<>();
        LegacyRegistryEntry registryEntry = new LegacyRegistryEntry(resourceEntry, relationshipEntries);
        registryEntry.setParentRegistryEntry(parentEntry);
        registryEntry.initialize(moduleRegistry);
        resourceRegistry.addEntry(resourceClass, registryEntry);

        allocateRepositoryRelations(registryEntry);

        JsonApiResource annotation = resourceClass.getAnnotation(JsonApiResource.class);
        if (annotation != null) {
            for (Class subType : annotation.subTypes()) {
                allocateRepository(subType, registryEntry);
            }
        }

        return registryEntry;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void allocateRepositoryRelations(RegistryEntry registryEntry) {
        ResourceInformation resourceInformation = registryEntry.getResourceInformation();
        List<ResourceField> relationshipFields = resourceInformation.getRelationshipFields();
        for (ResourceField relationshipField : relationshipFields) {
            final Class<?> targetClass = relationshipField.getElementType();
            Class sourceClass = resourceInformation.getResourceClass();
            allocateRepositoryRelation(sourceClass, targetClass);
        }
    }

    private RelationshipRepositoryAdapter allocateRepositoryRelation(Class sourceClass, Class targetClass) {
        // allocate relations as well
        ClientResourceRegistry clientResourceRegistry = (ClientResourceRegistry) resourceRegistry;
        if (!clientResourceRegistry.isInitialized(sourceClass)) {
            allocateRepository(sourceClass, null);
        }
        if (!clientResourceRegistry.isInitialized(targetClass)) {
            allocateRepository(targetClass, null);
        }

        LegacyRegistryEntry sourceEntry = (LegacyRegistryEntry) resourceRegistry.getEntry(sourceClass);
        final LegacyRegistryEntry targetEntry = (LegacyRegistryEntry) resourceRegistry.getEntry(targetClass);
        String targetResourceType = targetEntry.getResourceInformation().getResourceType();

        Map relationshipEntries = sourceEntry.getRelationshipEntries();
        DirectResponseRelationshipEntry relationshipEntry =
                (DirectResponseRelationshipEntry) relationshipEntries.get(targetResourceType);

        if (!relationshipEntries.containsKey(targetResourceType)) {

            final Object relationshipRepositoryStub = decorate(
                    new RelationshipRepositoryStubImpl(this, sourceClass, targetClass, sourceEntry.getResourceInformation(), urlBuilder)
            );
            RepositoryInstanceBuilder<LegacyRelationshipRepository> relationshipRepositoryInstanceBuilder =
                    new RepositoryInstanceBuilder(null, null) {

                        @Override
                        public Object buildRepository() {
                            return relationshipRepositoryStub;
                        }
                    };
            relationshipEntry = new DirectResponseRelationshipEntry(relationshipRepositoryInstanceBuilder);
            relationshipEntries.put(targetResourceType, relationshipEntry);
        }
        Object repoInstance = relationshipEntry.getRepositoryInstanceBuilder();

        return new RelationshipRepositoryAdapterImpl(null, moduleRegistry, repoInstance);
    }


    @SuppressWarnings("unchecked")
    public <R extends ResourceRepository<?, ?>> R getRepositoryForInterface(Class<R> repositoryInterfaceClass) {
        init();
        RepositoryInformationProvider informationBuilder = moduleRegistry.getRepositoryInformationBuilder();
        PreconditionUtil.verify(informationBuilder.accept(repositoryInterfaceClass), "%s is not a valid repository interface",
                repositoryInterfaceClass);
        ResourceRepositoryInformation repositoryInformation = (ResourceRepositoryInformation) informationBuilder
                .build(repositoryInterfaceClass, new DefaultRepositoryInformationProviderContext(moduleRegistry));
        Class<?> resourceClass = repositoryInformation.getResourceInformation().get().getResourceClass();

        Object actionStub = actionStubFactory != null ? actionStubFactory.createStub(repositoryInterfaceClass) : null;
        ResourceRepository<?, Serializable> repositoryStub = getRepositoryForType(resourceClass);

        ClassLoader classLoader = repositoryInterfaceClass.getClassLoader();
        InvocationHandler invocationHandler =
                new ClientStubInvocationHandler(repositoryInterfaceClass, repositoryStub, actionStub);
        return (R) Proxy.newProxyInstance(classLoader, new Class[]{repositoryInterfaceClass, ResourceRepository.class},
                invocationHandler);
    }

    /**
     * @param resourceClass repository class
     * @return stub for the given resourceClass
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T, I> ResourceRepository<T, I> getRepositoryForType(Class<T> resourceClass) {
        init();

        RegistryEntry entry = resourceRegistry.findEntry(resourceClass);
        ResourceRepositoryAdapter repositoryAdapter = entry.getResourceRepository();
        return (ResourceRepository<T, I>) repositoryAdapter.getResourceRepository();

    }

    /**
     * Generic access using {@link Resource} class without type mapping.
     */
    public ResourceRepository<Resource, String> getRepositoryForPath(String resourceType) {
        init();

        ResourceInformation resourceInformation =
                new ResourceInformation(
                        moduleRegistry.getTypeParser()
                        , Resource.class
                        , resourceType
                        , null
                        , null
                        , PagingSpec.class
                );
        return (ResourceRepository<Resource, String>) decorate(new ResourceRepositoryStubImpl<>(this, Resource.class, resourceInformation, urlBuilder));
    }

    /**
     * Generic access using {@link Resource} class without type mapping.
     */
    public RelationshipRepository<Resource, String, Resource, String> getRepositoryForPath(String sourceResourceType,
                                                                                           String targetResourceType) {
        init();

        ResourceInformation sourceResourceInformation =
                new ResourceInformation(moduleRegistry.getTypeParser(), Resource.class, sourceResourceType, null, null,
                        PagingSpec.class);
        return (RelationshipRepository<Resource, String, Resource, String>) decorate(new RelationshipRepositoryStubImpl<>(this, Resource.class, Resource.class, sourceResourceInformation, urlBuilder));
    }


    protected Object decorate(Object repository) {
        DefaultRegistryEntryBuilder entryBuilder = new DefaultRegistryEntryBuilder(moduleRegistry);
        return entryBuilder.decorateRepository(repository);
    }


    /**
     * @param sourceClass source class
     * @param targetClass target class
     * @return stub for the relationship between the given source and target
     * class
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T, I, D, J> RelationshipRepository<T, I, D, J> getRepositoryForType(
            Class<T> sourceClass, Class<D> targetClass) {
        init();

        RelationshipRepositoryAdapter repositoryAdapter = allocateRepositoryRelation(sourceClass, targetClass);
        return (RelationshipRepository<T, I, D, J>) repositoryAdapter.getRelationshipRepository();
    }

    /**
     * @param sourceClass source class
     * @param targetClass target class
     * @return stub for the relationship between the given source and target
     * class
     */
    public <T, I, D, J> ManyRelationshipRepository<T, I, D, J> getManyRepositoryForType(
            Class<T> sourceClass, Class<D> targetClass) {
        init();

        RelationshipRepositoryAdapter repositoryAdapter = allocateRepositoryRelation(sourceClass, targetClass);
        return (ManyRelationshipRepository<T, I, D, J>) repositoryAdapter.getRelationshipRepository();
    }

    /**
     * @param sourceClass source class
     * @param targetClass target class
     * @return stub for the relationship between the given source and target
     * class
     */
    public <T, I, D, J> OneRelationshipRepository<T, I, D, J> getOneRepositoryForType(
            Class<T> sourceClass, Class<D> targetClass) {
        init();

        RelationshipRepositoryAdapter repositoryAdapter = allocateRepositoryRelation(sourceClass, targetClass);
        return (OneRelationshipRepository<T, I, D, J>) repositoryAdapter.getRelationshipRepository();
    }

    /**
     * @return objectMapper in use
     */
    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    /**
     * @return resource registry use.
     */
    public ResourceRegistry getRegistry() {
        init();
        return resourceRegistry;
    }

    /**
     * Adds the given module.
     */
    public void addModule(Module module) {
        PreconditionUtil.verify(!initialized, "CrnkClient already initialized, cannot add further modules");
        if (module instanceof HttpAdapterAware) {
            ((HttpAdapterAware) module).setHttpAdapter(getHttpAdapter());
        }
        this.moduleRegistry.addModule(module);
    }

    public HttpAdapter getHttpAdapter() {
        initHttpAdapter();
        return httpAdapter;
    }

    public void setHttpAdapter(HttpAdapter httpAdapter) {
        this.httpAdapter = httpAdapter;

        List<Module> modules = moduleRegistry.getModules();
        for (Module module : modules) {
            if (module instanceof HttpAdapterAware) {
                ((HttpAdapterAware) module).setHttpAdapter(getHttpAdapter());
            }
        }
    }

    public ExceptionMapperRegistry getExceptionMapperRegistry() {
        init();
        return exceptionMapperRegistry;
    }

    public ActionStubFactory getActionStubFactory() {
        return actionStubFactory;
    }

    /**
     * Sets the factory to use to create action stubs (like JAX-RS annotated
     * repository methods).
     *
     * @param actionStubFactory to use
     */
    public void setActionStubFactory(ActionStubFactory actionStubFactory) {
        this.actionStubFactory = actionStubFactory;
        if (actionStubFactory != null) {
            actionStubFactory.init(new ActionStubFactoryContext() {

                @Override
                public ServiceUrlProvider getServiceUrlProvider() {
                    return moduleRegistry.getHttpRequestContextProvider().getServiceUrlProvider();
                }

                @Override
                public HttpAdapter getHttpAdapter() {
                    return httpAdapter;
                }
            });
        }
    }

    public QuerySpecUrlMapper getUrlMapper() {
        return moduleRegistry.getUrlMapper();
    }

    public void setUrlMapper(QuerySpecUrlMapper urlMapper) {
        moduleRegistry.setUrlMapper(urlMapper);
    }

    public ModuleRegistry getModuleRegistry() {
        return moduleRegistry;
    }

    public ClientDocumentMapper getDocumentMapper() {
        return documentMapper;
    }

    public ServiceUrlProvider getServiceUrlProvider() {
        return moduleRegistry.getHttpRequestContextProvider().getServiceUrlProvider();
    }

    class ClientResourceRegistry extends ResourceRegistryImpl {

        public ClientResourceRegistry(ModuleRegistry moduleRegistry) {
            super(new DefaultResourceRegistryPart(), moduleRegistry);
        }

        @Override
        protected synchronized RegistryEntry findEntry(Class<?> clazz, boolean allowNull) {
            RegistryEntry entry = getEntry(clazz);
            if (entry == null) {
                ResourceInformationProvider informationBuilder = moduleRegistry.getResourceInformationBuilder();
                if (!informationBuilder.accept(clazz)) {
                    throw new InvalidResourceException(clazz.getName() + " not recognized as resource class, consider adding "
                            + "@JsonApiResource annotation");
                }
                entry = allocateRepository(clazz, null);
            }
            return entry;
        }

        public boolean isInitialized(Class<?> clazz) {
            return super.getEntry(clazz) != null;
        }
    }
}
