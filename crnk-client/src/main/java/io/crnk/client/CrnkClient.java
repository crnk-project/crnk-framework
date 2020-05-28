package io.crnk.client;

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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.crnk.client.action.ActionStubFactory;
import io.crnk.client.action.ActionStubFactoryContext;
import io.crnk.client.http.HttpAdapter;
import io.crnk.client.http.HttpAdapterProvider;
import io.crnk.client.http.apache.HttpClientAdapterProvider;
import io.crnk.client.http.okhttp.OkHttpAdapterProvider;
import io.crnk.client.internal.ClientDocumentMapper;
import io.crnk.client.internal.ClientHttpRequestContext;
import io.crnk.client.internal.ClientStubInvocationHandler;
import io.crnk.client.internal.RelationshipRepositoryStubImpl;
import io.crnk.client.internal.ResourceRepositoryStubImpl;
import io.crnk.client.internal.SingletonResultFactory;
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
import io.crnk.core.engine.internal.registry.RegistryEntryImpl;
import io.crnk.core.engine.internal.registry.ResourceRegistryImpl;
import io.crnk.core.engine.internal.repository.RelationshipRepositoryAdapter;
import io.crnk.core.engine.internal.repository.RelationshipRepositoryAdapterImpl;
import io.crnk.core.engine.internal.repository.ResourceRepositoryAdapter;
import io.crnk.core.engine.internal.repository.ResourceRepositoryAdapterImpl;
import io.crnk.core.engine.internal.utils.ClassUtils;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.engine.internal.utils.UrlUtils;
import io.crnk.core.engine.properties.NullPropertiesProvider;
import io.crnk.core.engine.properties.PropertiesProvider;
import io.crnk.core.engine.properties.SystemPropertiesProvider;
import io.crnk.core.engine.query.QueryContext;
import io.crnk.core.engine.registry.DefaultResourceRegistryPart;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.engine.url.ConstantServiceUrlProvider;
import io.crnk.core.engine.url.ServiceUrlProvider;
import io.crnk.core.exception.InvalidResourceException;
import io.crnk.core.module.Module;
import io.crnk.core.module.ModuleRegistry;
import io.crnk.core.module.discovery.DefaultServiceDiscoveryFactory;
import io.crnk.core.module.discovery.ResourceLookup;
import io.crnk.core.module.discovery.ServiceDiscovery;
import io.crnk.core.module.discovery.ServiceDiscoveryFactory;
import io.crnk.core.module.internal.DefaultRepositoryInformationProviderContext;
import io.crnk.core.module.internal.ModuleUtils;
import io.crnk.core.queryspec.mapper.DefaultQuerySpecUrlMapper;
import io.crnk.core.queryspec.mapper.QuerySpecUrlMapper;
import io.crnk.core.queryspec.mapper.UrlBuilder;
import io.crnk.core.queryspec.pagingspec.LimitBoundedPagingBehavior;
import io.crnk.core.queryspec.pagingspec.OffsetLimitPagingBehavior;
import io.crnk.core.queryspec.pagingspec.PagingBehavior;
import io.crnk.core.queryspec.pagingspec.PagingSpec;
import io.crnk.core.repository.ManyRelationshipRepository;
import io.crnk.core.repository.OneRelationshipRepository;
import io.crnk.core.repository.RelationshipRepository;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.core.repository.decorate.Wrapper;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.core.resource.annotations.JsonApiVersion;
import io.crnk.core.resource.list.DefaultResourceList;

/**
 * Client implementation giving access to JSON API repositories using stubs.
 */
public class CrnkClient {

	private static final String REST_TEMPLATE_PROVIDER_NAME = "io.crnk.spring.client.RestTemplateAdapterProvider";

	private final ClientType clientType;

	private HttpAdapter httpAdapter;

	private ObjectMapper objectMapper;

	private ResourceRegistry resourceRegistry;

	private ModuleRegistry moduleRegistry;

	private UrlBuilder urlBuilder;

	private boolean initialized = false;

	private ExceptionMapperRegistry exceptionMapperRegistry;

	private ActionStubFactory actionStubFactory;

	private ClientDocumentMapper documentMapper;

	private ServiceDiscovery serviceDiscovery;

	private ServiceDiscoveryFactory serviceDiscoveryFactory = new DefaultServiceDiscoveryFactory();

	private PropertiesProvider propertiesProvider = new SystemPropertiesProvider();

	private Long defaultPageLimit = null;

	private Long maxPageLimit = null;

	private QueryContext queryContext = new QueryContext().setRequestVersion(Integer.MAX_VALUE);

	private List<HttpAdapterProvider> httpAdapterProviders = new ArrayList<>();

	private ClientFormat format = ClientFormat.JSONAPI;

	private ClientProxyFactory configuredProxyFactory;

	public enum ClientType {
		SIMPLE_lINKS,

		/**
		 * @deprecated currently barely used, speak up if still necessary
		 */
		@Deprecated
		OBJECT_LINKS
	}

	public CrnkClient(String serviceUrl) {
		this(new ConstantServiceUrlProvider(UrlUtils.removeTrailingSlash(serviceUrl)), ClientType.SIMPLE_lINKS);
	}

	public CrnkClient(String serviceUrl, ClientType clientType) {
		this(new ConstantServiceUrlProvider(UrlUtils.removeTrailingSlash(serviceUrl)), clientType);
	}

	public CrnkClient(ServiceUrlProvider serviceUrlProvider, ClientType clientType) {
		this.clientType = clientType;
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

		moduleRegistry.setResultFactory(new SingletonResultFactory());
		moduleRegistry.addModule(new ResourceInformationProviderModule());

		resourceRegistry = new ClientResourceRegistry(moduleRegistry);
		queryContext.setBaseUrl(serviceUrlProvider.getUrl());
		urlBuilder = moduleRegistry.getUrlBuilder();


		setProxyFactory(new BasicProxyFactory());
	}

	public int getVersion() {
		return queryContext.getRequestVersion();
	}

	/**
	 * @param version to sent along with ACCEPT header. For more information see {@link JsonApiVersion}.
	 */
	public void setVersion(int version) {
		this.queryContext.setRequestVersion(version);
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
			serviceDiscovery = serviceDiscoveryFactory.getInstance();
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
	 * Finds and registers modules on the classpath trough the use of java.util.ServiceLoader. Each module can register itself for lookup by registering a ClientModuleFactory.
	 */
	public void findModules() {
		initObjectMapper();
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
		this.configuredProxyFactory = proxyFactory;
		if (documentMapper != null) {
			documentMapper.setProxyFactory(proxyFactory);
		}
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
		initObjectMapper();
		configureObjectMapper();
		initModuleRegistry();
		initExceptionMapperRegistry();
		initResources();

		moduleRegistry.getHttpRequestContextProvider().onRequestStarted(new ClientHttpRequestContext(queryContext));

		Optional<Module> plainJsonModule = moduleRegistry.getModules().stream().filter(it -> it.getModuleName().equals("plain-json")).findFirst();
		if (plainJsonModule.isPresent()) {
			format = ClientFormat.PLAINJSON;
		}
	}

	protected void initObjectMapper() {
		if (objectMapper == null) {
			objectMapper = createDefaultObjectMapper();
			configureObjectMapper();
		}

	}

	protected ObjectMapper createDefaultObjectMapper() {
		ObjectMapper om = new ObjectMapper();
		om.enable(SerializationFeature.INDENT_OUTPUT);
		return om;
	}

	protected void configureObjectMapper() {
		initJacksonModule(clientType == ClientType.OBJECT_LINKS);
	}

	protected void initHttpAdapter() {
		if (httpAdapter == null) {
			httpAdapter = detectHttpAdapter();
		}
	}

	protected void initResources() {
		ResourceLookup resourceLookup = moduleRegistry.getResourceLookup();
		for (Class<?> resourceClass : resourceLookup.getResourceClasses()) {
			getRepositoryForType(resourceClass);
		}
	}

	protected void initModuleRegistry() {
		moduleRegistry.init(objectMapper);

		switch (clientType) {
			case OBJECT_LINKS:
				documentMapper = new ClientDocumentMapper(moduleRegistry, objectMapper, key -> {
					if (key.equals(CrnkProperties.SERIALIZE_LINKS_AS_OBJECTS)) {
						return "true";
					}
					return null;
				});
				break;
			default:
				documentMapper = new ClientDocumentMapper(moduleRegistry, objectMapper, new NullPropertiesProvider());
		}
		if (configuredProxyFactory != null) {
			documentMapper.setProxyFactory(configuredProxyFactory);
		}
	}

	protected void initExceptionMapperRegistry() {
		ExceptionMapperLookup exceptionMapperLookup = moduleRegistry.getExceptionMapperLookup();
		exceptionMapperRegistry = new ExceptionMapperRegistryBuilder().build(exceptionMapperLookup);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected <T, I> RegistryEntry allocateRepository(Class<T> resourceClass, RegistryEntry parentEntry) {
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
		DefaultRegistryEntryBuilder.contributeFields(moduleRegistry, resourceInformation);
		ModuleUtils.adaptInformation(resourceInformation, moduleRegistry);

		final ResourceRepository repositoryStub = (ResourceRepository) decorate(
				new ResourceRepositoryStubImpl<T, I>(this, resourceClass, resourceInformation, urlBuilder)
		);

		// create interface for it!
		ResourceRepositoryInformation repositoryInformation =
				new ResourceRepositoryInformationImpl(resourceInformation.getResourceType(),
						resourceInformation, new HashMap<>(), RepositoryMethodAccess.ALL, true);
		ResourceRepositoryAdapter resourceRepositoryAdapter = new ResourceRepositoryAdapterImpl(repositoryInformation, moduleRegistry, repositoryStub);
		Map<ResourceField, RelationshipRepositoryAdapter> relationshipRepositoryAdapters = new HashMap<>();
		RegistryEntryImpl registryEntry = new RegistryEntryImpl(resourceInformation, resourceRepositoryAdapter,
				relationshipRepositoryAdapters,
				moduleRegistry);

		registryEntry.setParentRegistryEntry(parentEntry);
		resourceRegistry.addEntry(registryEntry);

		allocateRepositoryRelations(registryEntry);

		JsonApiResource annotation = resourceClass.getAnnotation(JsonApiResource.class);
		if (annotation != null) {
			for (Class subType : annotation.subTypes()) {
				allocateRepository(subType, registryEntry);
			}
		}

		moduleRegistry.initOpposites(true); // client only has partial knowledge o
		if (resourceInformation.getIdField() != null) {
			resourceInformation.initNesting();
		}

		return registryEntry;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void allocateRepositoryRelations(RegistryEntry registryEntry) {
		ResourceInformation resourceInformation = registryEntry.getResourceInformation();
		List<ResourceField> relationshipFields = resourceInformation.getRelationshipFields();
		for (ResourceField relationshipField : relationshipFields) {
			allocateRepositoryRelation(relationshipField);
		}
	}

	private RelationshipRepositoryAdapter allocateRepositoryRelation(Class sourceClass, Class targetClass) {
		RegistryEntry sourceEntry = allocateRepository(sourceClass, null);
		for (ResourceField field : sourceEntry.getResourceInformation().getRelationshipFields()) {
			if (field.getElementType() == targetClass) {
				return allocateRepositoryRelation(field);
			}
		}
		throw new IllegalArgumentException("no relationship found between " + sourceClass + " " + targetClass);
	}


	private RelationshipRepositoryAdapter allocateRepositoryRelation(ResourceField field) {
		// allocate relations as well
		ClientResourceRegistry clientResourceRegistry = (ClientResourceRegistry) resourceRegistry;
		Class<?> sourceClass = field.getResourceInformation().getImplementationClass();
		Class<?> targetClass = field.getElementType();
		if (!clientResourceRegistry.isInitialized(sourceClass)) {
			allocateRepository(sourceClass, null);
		}
		if (!clientResourceRegistry.isInitialized(targetClass)) {
			allocateRepository(targetClass, null);
		}

		RegistryEntryImpl sourceEntry = (RegistryEntryImpl) resourceRegistry.getEntry(sourceClass);
		if (sourceEntry.hasRelationship(field)) {
			return sourceEntry.getRelationshipRepository(field);
		}

		final Object relationshipRepositoryStub = decorate(
				new RelationshipRepositoryStubImpl(this, sourceClass, targetClass, sourceEntry.getResourceInformation(), urlBuilder)
		);

		RelationshipRepositoryAdapter adapter = new RelationshipRepositoryAdapterImpl(field, moduleRegistry, relationshipRepositoryStub);
		sourceEntry.putRelationshipRepository(field, adapter);
		return adapter;
	}


	@SuppressWarnings("unchecked")
	public <R extends ResourceRepository<?, ?>> R getRepositoryForInterface(Class<R> repositoryInterfaceClass) {
		init();
		RepositoryInformationProvider informationBuilder = moduleRegistry.getRepositoryInformationBuilder();
		PreconditionUtil.verify(informationBuilder.accept(repositoryInterfaceClass), "%s is not a valid repository interface", repositoryInterfaceClass);
		ResourceRepositoryInformation repositoryInformation =
				(ResourceRepositoryInformation) informationBuilder.build(repositoryInterfaceClass, new DefaultRepositoryInformationProviderContext(moduleRegistry));
		Class<?> resourceClass = repositoryInformation.getResource().getResourceClass();

		Object actionStub = actionStubFactory != null ? actionStubFactory.createStub(repositoryInterfaceClass) : null;
		ResourceRepository<?, Serializable> repositoryStub = getRepositoryForType(resourceClass);

		ClassLoader classLoader = repositoryInterfaceClass.getClassLoader();
		InvocationHandler invocationHandler =
				new ClientStubInvocationHandler(repositoryInterfaceClass, repositoryStub, actionStub);
		return (R) Proxy.newProxyInstance(classLoader, new Class[] { repositoryInterfaceClass, Wrapper.class, ResourceRepository.class },
				invocationHandler);
	}

	/**
	 * @param resourceClass repository class
	 * @return stub for the given resourceClass
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T, I> ResourceRepository<T, I> getRepositoryForType(Class<T> resourceClass) {
		init();

		RegistryEntry entry = resourceRegistry.findEntry(resourceClass);
		ResourceRepositoryAdapter repositoryAdapter = entry.getResourceRepository();
		return (ResourceRepository<T, I>) repositoryAdapter.getImplementation();

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
		return (RelationshipRepository<Resource, String, Resource, String>) decorate(
				new RelationshipRepositoryStubImpl<>(this, Resource.class, Resource.class, sourceResourceInformation,
						urlBuilder));
	}


	protected Object decorate(Object repository) {
		DefaultRegistryEntryBuilder entryBuilder = new DefaultRegistryEntryBuilder(moduleRegistry);
		return entryBuilder.decorateRepository(repository);
	}


	/**
	 * @param sourceClass source class
	 * @param targetClass target class
	 * @return stub for the relationship between the given source and target class
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T, I, D, J> RelationshipRepository<T, I, D, J> getRepositoryForType(
			Class<T> sourceClass, Class<D> targetClass) {
		init();

		RelationshipRepositoryAdapter repositoryAdapter = allocateRepositoryRelation(sourceClass, targetClass);
		return (RelationshipRepository<T, I, D, J>) repositoryAdapter.getImplementation();
	}

	/**
	 * @param sourceClass source class
	 * @param targetClass target class
	 * @return stub for the relationship between the given source and target class
	 */
	public <T, I, D, J> ManyRelationshipRepository<T, I, D, J> getManyRepositoryForType(
			Class<T> sourceClass, Class<D> targetClass) {
		init();

		RelationshipRepositoryAdapter repositoryAdapter = allocateRepositoryRelation(sourceClass, targetClass);
		return (ManyRelationshipRepository<T, I, D, J>) repositoryAdapter.getImplementation();
	}

	/**
	 * @param sourceClass source class
	 * @param targetClass target class
	 * @return stub for the relationship between the given source and target class
	 */
	public <T, I, D, J> OneRelationshipRepository<T, I, D, J> getOneRepositoryForType(
			Class<T> sourceClass, Class<D> targetClass) {
		init();

		RelationshipRepositoryAdapter repositoryAdapter = allocateRepositoryRelation(sourceClass, targetClass);
		return (OneRelationshipRepository<T, I, D, J>) repositoryAdapter.getImplementation();
	}

	/**
	 * @return objectMapper in use
	 */
	public ObjectMapper getObjectMapper() {
		initObjectMapper();
		return objectMapper;
	}

	public void setObjectMapper(ObjectMapper objectMapper) {
		PreconditionUtil.verify(this.objectMapper == null, "ObjectMapper already configured, consider calling SetObjectMapper earlier or and avoid multiple calls");
		this.objectMapper = objectMapper;
		this.configureObjectMapper();
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
	 * Sets the factory to use to create action stubs (like JAX-RS annotated repository methods).
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
		init();
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
