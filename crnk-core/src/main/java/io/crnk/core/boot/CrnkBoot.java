package io.crnk.core.boot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.crnk.core.engine.error.JsonApiExceptionMapper;
import io.crnk.core.engine.filter.DocumentFilter;
import io.crnk.core.engine.filter.ResourceFilterDirectory;
import io.crnk.core.engine.http.HttpRequestContextAware;
import io.crnk.core.engine.information.resource.ResourceInformationProviderModule;
import io.crnk.core.engine.internal.CoreModule;
import io.crnk.core.engine.internal.dispatcher.ControllerRegistry;
import io.crnk.core.engine.internal.dispatcher.controller.CollectionGet;
import io.crnk.core.engine.internal.dispatcher.controller.Controller;
import io.crnk.core.engine.internal.dispatcher.controller.ControllerContext;
import io.crnk.core.engine.internal.dispatcher.controller.FieldResourceGet;
import io.crnk.core.engine.internal.dispatcher.controller.FieldResourcePost;
import io.crnk.core.engine.internal.dispatcher.controller.RelationshipsResourceDelete;
import io.crnk.core.engine.internal.dispatcher.controller.RelationshipsResourceGet;
import io.crnk.core.engine.internal.dispatcher.controller.RelationshipsResourcePatch;
import io.crnk.core.engine.internal.dispatcher.controller.RelationshipsResourcePost;
import io.crnk.core.engine.internal.dispatcher.controller.ResourceDelete;
import io.crnk.core.engine.internal.dispatcher.controller.ResourceGet;
import io.crnk.core.engine.internal.dispatcher.controller.ResourcePatch;
import io.crnk.core.engine.internal.dispatcher.controller.ResourcePost;
import io.crnk.core.engine.internal.document.mapper.DocumentMapper;
import io.crnk.core.engine.internal.exception.ExceptionMapperRegistry;
import io.crnk.core.engine.internal.http.HttpRequestDispatcherImpl;
import io.crnk.core.engine.internal.http.JsonApiRequestProcessor;
import io.crnk.core.engine.internal.jackson.JacksonModule;
import io.crnk.core.engine.internal.registry.ResourceRegistryImpl;
import io.crnk.core.engine.internal.utils.ClassUtils;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.engine.properties.NullPropertiesProvider;
import io.crnk.core.engine.properties.PropertiesProvider;
import io.crnk.core.engine.query.QueryAdapterBuilder;
import io.crnk.core.engine.registry.DefaultResourceRegistryPart;
import io.crnk.core.engine.registry.HierarchicalResourceRegistryPart;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.engine.registry.ResourceRegistryPart;
import io.crnk.core.engine.result.ResultFactory;
import io.crnk.core.engine.url.ConstantServiceUrlProvider;
import io.crnk.core.engine.url.ServiceUrlProvider;
import io.crnk.core.module.Module;
import io.crnk.core.module.ModuleRegistry;
import io.crnk.core.module.SimpleModule;
import io.crnk.core.module.discovery.DefaultServiceDiscoveryFactory;
import io.crnk.core.module.discovery.FallbackServiceDiscoveryFactory;
import io.crnk.core.module.discovery.ServiceDiscovery;
import io.crnk.core.module.discovery.ServiceDiscoveryFactory;
import io.crnk.core.queryspec.QuerySpecDeserializer;
import io.crnk.core.queryspec.internal.QuerySpecAdapterBuilder;
import io.crnk.core.queryspec.internal.UrlMapperAdapter;
import io.crnk.core.queryspec.mapper.DefaultQuerySpecUrlMapper;
import io.crnk.core.queryspec.mapper.QuerySpecUrlMapper;
import io.crnk.core.queryspec.mapper.UnkonwnMappingAware;
import io.crnk.core.queryspec.pagingspec.OffsetLimitPagingBehavior;
import io.crnk.core.queryspec.pagingspec.PagingBehavior;
import io.crnk.core.repository.Repository;
import io.crnk.legacy.internal.QueryParamsAdapterBuilder;
import io.crnk.legacy.locator.JsonServiceLocator;
import io.crnk.legacy.locator.SampleJsonServiceLocator;
import io.crnk.legacy.queryParams.QueryParamsBuilder;
import io.crnk.legacy.repository.annotations.JsonApiRelationshipRepository;
import io.crnk.legacy.repository.annotations.JsonApiResourceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Facilitates the startup of Crnk in various environments (Spring, CDI,
 * JAX-RS, etc.).
 */
@SuppressWarnings("deprecation")
public class CrnkBoot {

	private static final Logger LOGGER = LoggerFactory.getLogger(CrnkBoot.class);

	private static final String DISCOVERY_MODULE_NAME = "discovery";

	private final ModuleRegistry moduleRegistry = new ModuleRegistry();

	private ObjectMapper objectMapper;

	private QueryParamsBuilder queryParamsBuilder;

	private boolean configured;

	private JsonServiceLocator serviceLocator = new SampleJsonServiceLocator();

	private ResourceRegistry resourceRegistry;

	private HttpRequestDispatcherImpl requestDispatcher;

	private PropertiesProvider propertiesProvider = new NullPropertiesProvider();

	private ServiceDiscoveryFactory serviceDiscoveryFactory = new DefaultServiceDiscoveryFactory();

	private ServiceDiscovery serviceDiscovery;

	private DocumentMapper documentMapper;

	private List<Module> registeredModules = new ArrayList<>();

	private Long defaultPageLimit = null;

	private Long maxPageLimit = null;

	private Boolean allowUnknownAttributes;

	private Boolean allowUnknownParameters;

	private QueryAdapterBuilder queryAdapterBuilder;

	private CoreModule coreModule = new CoreModule();

	private Map<String, String> serverInfo = new HashMap<>();

	private String webPathPrefix;

	private static String buildServiceUrl(String resourceDefaultDomain, String webPathPrefix) {
		return resourceDefaultDomain + (webPathPrefix != null ? webPathPrefix : "");
	}

	/**
	 * Returned in the jsonapi field with every response. See http://jsonapi.org/format/#document-top-level.
	 */
	public void putServerInfo(String key, String value) {
		serverInfo.put(key, value);

		moduleRegistry.setServerInfo(serverInfo);
	}

	public void setServiceDiscoveryFactory(ServiceDiscoveryFactory factory) {
		checkNotConfiguredYet();
		PreconditionUtil.verify(serviceDiscovery == null, "serviceDiscovery already initialized: %s", serviceDiscovery);
		this.serviceDiscoveryFactory = factory;
	}

	/**
	 * Set the {@link QueryParamsBuilder} to use to parse and handle query parameters.
	 * When invoked, overwrites previous QueryParamsBuilders and {@link QuerySpecDeserializer}s.
	 */
	public void setQueryParamsBuilds(QueryParamsBuilder queryParamsBuilder) {
		checkNotConfiguredYet();
		PreconditionUtil.verify(queryParamsBuilder != null, "A query params builder must be provided, but was null");
		this.queryParamsBuilder = queryParamsBuilder;
		moduleRegistry.setUrlMapper(null);
	}

	/**
	 * Sets a JsonServiceLocator. No longer necessary if a ServiceDiscovery
	 * implementation is in place.
	 */
	public void setServiceLocator(JsonServiceLocator serviceLocator) {
		checkNotConfiguredYet();
		this.serviceLocator = serviceLocator;
	}


	/**
	 * Adds a module. No longer necessary if a ServiceDiscovery implementation
	 * is in place.
	 */
	public void addModule(Module module) {
		checkNotConfiguredYet();
		setupInstance(module);
		registeredModules.add(module);
	}

	/**
	 * Sets a ServiceUrlProvider. No longer necessary if a ServiceDiscovery
	 * implementation is in place.
	 */
	public void setServiceUrlProvider(ServiceUrlProvider serviceUrlProvider) {
		checkNotConfiguredYet();
		this.moduleRegistry.getHttpRequestContextProvider().setServiceUrlProvider(serviceUrlProvider);
	}

	private void checkNotConfiguredYet() {
		if (configured) {
			throw new IllegalStateException("cannot further modify CrnkBoot once configured/initialized");
		}
	}

	/**
	 * Performs the setup.
	 */
	public void boot() {
		LOGGER.debug("performing setup");
		checkNotConfiguredYet();
		configured = true;

		// Set the properties provider into the registry early
		// so that it is available to the modules being bootstrapped
		moduleRegistry.setPropertiesProvider(propertiesProvider);

		setupServiceUrlProvider();
		setupServiceDiscovery();
		setupQuerySpecUrlMapper();
		setupPagingBehavior();
		bootDiscovery();

		LOGGER.debug("completed setup");
	}

	private void setupServiceDiscovery() {
		if (serviceDiscovery == null) {
			// revert to reflection-based approach if no ServiceDiscovery is
			// found
			FallbackServiceDiscoveryFactory fallback =
					new FallbackServiceDiscoveryFactory(serviceDiscoveryFactory, serviceLocator, propertiesProvider);
			setServiceDiscovery(fallback.getInstance());
		}
	}

	private void bootDiscovery() {
		setupObjectMapper();

		resourceRegistry = new ResourceRegistryImpl(null, moduleRegistry);

		addModules();

		setupComponents();
		ResourceRegistryPart rootPart = setupResourceRegistry();

		moduleRegistry.init(objectMapper);

		setupRepositories(rootPart);

		requestDispatcher = createRequestDispatcher(moduleRegistry.getExceptionMapperRegistry());

		logInfo();
	}

	private void logInfo() {
		int numResources = resourceRegistry.getResources().size();
		List<String> modules = moduleRegistry.getModules().stream().map(Module::getModuleName).collect(Collectors.toList());
		// hide internal module names
		modules.remove(ResourceInformationProviderModule.NAME);
		modules.remove(DISCOVERY_MODULE_NAME);
		modules.remove(CoreModule.NAME);
		List<String> securityProviders = toSimpleNames(moduleRegistry.getSecurityProviders());
		List<String> pagingBehaviors = toSimpleNames(moduleRegistry.getPagingBehaviors());
		QuerySpecUrlMapper urlMapper = moduleRegistry.getUrlMapper();
		ServiceDiscovery serviceDiscovery = moduleRegistry.getServiceDiscovery();

		LOGGER.info("crnk initialized: numResources={}, usedModules={}, securityProviders={}, pagingBehaviors={}, " +
						"urlMapper={}, serviceDiscovery={}", numResources, modules, securityProviders, pagingBehaviors,
				urlMapper.getClass().getSimpleName(),
				serviceDiscovery.getClass().getSimpleName());

		if (numResources == 0) {
			LOGGER.warn("no resources found");
		}
	}

	private List<String> toSimpleNames(List<?> implementations) {
		return implementations.stream().map(it -> it.getClass().getSimpleName()).collect(Collectors.toList());
	}

	private void setupRepositories(ResourceRegistryPart rootPart) {
		for (RegistryEntry entry : moduleRegistry.getRegistryEntries()) {
			rootPart.addEntry(entry);
		}
	}

	private ResourceRegistryPart setupResourceRegistry() {
		Map<String, ResourceRegistryPart> registryParts = moduleRegistry.getRegistryParts();

		ResourceRegistryPart rootPart;
		if (registryParts.isEmpty()) {
			rootPart = new DefaultResourceRegistryPart();
		}
		else {
			HierarchicalResourceRegistryPart hierarchialPart = new HierarchicalResourceRegistryPart();
			for (Map.Entry<String, ResourceRegistryPart> entry : registryParts.entrySet()) {
				hierarchialPart.putPart(entry.getKey(), entry.getValue());
			}
			if (!registryParts.containsKey("")) {
				moduleRegistry.getContext().addRegistryPart("", new DefaultResourceRegistryPart());
			}
			rootPart = hierarchialPart;
		}
		((ResourceRegistryImpl) resourceRegistry).setRootPart(rootPart);

		return rootPart;
	}

	private void setupObjectMapper() {
		if (objectMapper == null) {
			objectMapper = new ObjectMapper();
			objectMapper.findAndRegisterModules();
			objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
		}
		objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

		moduleRegistry.setObjectMapper(objectMapper);
	}

	public ExceptionMapperRegistry getExceptionMapperRegistry() {
		return moduleRegistry.getExceptionMapperRegistry();
	}

	private HttpRequestDispatcherImpl createRequestDispatcher(ExceptionMapperRegistry exceptionMapperRegistry) {
		this.documentMapper = createDocumentMapper();
		return new HttpRequestDispatcherImpl(moduleRegistry, exceptionMapperRegistry);
	}

	protected QueryAdapterBuilder createQueryAdapterBuilder() {
		if (queryParamsBuilder != null) {
			return new QueryParamsAdapterBuilder(queryParamsBuilder, moduleRegistry);
		}
		else {
			return new QuerySpecAdapterBuilder(moduleRegistry.getUrlMapper(), moduleRegistry);
		}
	}

	protected DocumentMapper createDocumentMapper() {
		ResourceFilterDirectory filterDirectory = moduleRegistry.getContext().getResourceFilterDirectory();
		ResultFactory resultFactory = moduleRegistry.getContext().getResultFactory();
		return new DocumentMapper(resourceRegistry, objectMapper, propertiesProvider, filterDirectory, resultFactory,
				serverInfo);
	}

	protected ControllerRegistry createControllerRegistry() {
		Set<Controller> controllers = new HashSet<>();
		controllers.add(new RelationshipsResourceDelete());
		controllers.add(new RelationshipsResourcePatch());
		controllers.add(new RelationshipsResourcePost());
		controllers.add(new ResourceDelete());
		controllers.add(new CollectionGet());
		controllers.add(new FieldResourceGet());
		controllers.add(new RelationshipsResourceGet());
		controllers.add(new ResourceGet());
		controllers.add(new FieldResourcePost());
		controllers.add(new ResourcePatch());
		controllers.add(new ResourcePost());

		ControllerContext context = new ControllerContext(moduleRegistry, this::getDocumentMapper);
		for (Controller controller : controllers) {
			controller.init(context);
		}

		return new ControllerRegistry(controllers);
	}

	public DocumentMapper getDocumentMapper() {
		return documentMapper;
	}

	private ControllerRegistry controllerRegistry;

	private void setupComponents() {
		// not that the provided default implementation here are added last and
		// as a consequence,
		// can be overriden by other modules, like the
		// JaxrsResourceRepositoryInformationBuilder.
		LOGGER.debug("performing service discovery with {}", serviceDiscovery);
		SimpleModule module = new SimpleModule(DISCOVERY_MODULE_NAME) {

			@Override
			public void setupModule(ModuleContext context) {
				controllerRegistry = createControllerRegistry();
				queryAdapterBuilder = createQueryAdapterBuilder();

				this.addHttpRequestProcessor(new JsonApiRequestProcessor(context, controllerRegistry, queryAdapterBuilder));
				super.setupModule(context);
			}
		};

		for (JsonApiExceptionMapper<?> exceptionMapper : getInstancesByType(JsonApiExceptionMapper.class)) {
			module.addExceptionMapper(exceptionMapper);
		}
		for (DocumentFilter filter : getInstancesByType(DocumentFilter.class)) {
			module.addFilter(filter);
		}
		List<Repository> repositories = getInstancesByType(Repository.class);
		for (Object repository : repositories) {
			module.addRepository(repository);
		}
		for (Object repository : serviceDiscovery.getInstancesByAnnotation(JsonApiResourceRepository.class)) {
			JsonApiResourceRepository annotation =
					ClassUtils.getAnnotation(repository.getClass(), JsonApiResourceRepository.class).get();
			Class<?> resourceClass = annotation.value();
			module.addRepository(resourceClass, repository);
		}
		for (Object repository : serviceDiscovery.getInstancesByAnnotation(JsonApiRelationshipRepository.class)) {
			JsonApiRelationshipRepository annotation =
					ClassUtils.getAnnotation(repository.getClass(), JsonApiRelationshipRepository.class).get();
			module.addRepository(annotation.source(), annotation.target(), repository);
		}

		moduleRegistry.addModule(module);
		moduleRegistry.addModule(new ResourceInformationProviderModule());
	}

	private <T> List<T> getInstancesByType(Class<T> clazz) {
		List<T> instancesByType = serviceDiscovery.getInstancesByType(clazz);
		for (T instance : instancesByType) {
			setupInstance(instance);
		}
		return instancesByType;
	}

	private <T> void setupInstance(T instance) {
		if (instance instanceof HttpRequestContextAware) {
			HttpRequestContextAware aware = (HttpRequestContextAware) instance;
			aware.setHttpRequestContextProvider(moduleRegistry.getHttpRequestContextProvider());
		}
	}

	private void addModules() {
		boolean serializeLinksAsObjects =
				Boolean.parseBoolean(propertiesProvider.getProperty(CrnkProperties.SERIALIZE_LINKS_AS_OBJECTS));
		moduleRegistry.addModule(new JacksonModule(objectMapper, serializeLinksAsObjects));

		// without priority setup or something, has to come last as some defaults are in there, needs to become more robust
		moduleRegistry.addModule(coreModule);

		for (Module module : registeredModules) {
			moduleRegistry.addModule(module);
		}

		List<Module> discoveredModules = getInstancesByType(Module.class);
		for (Module module : discoveredModules) {
			moduleRegistry.addModule(module);
		}
	}

	private void setupServiceUrlProvider() {
		String resourceDefaultDomain = propertiesProvider.getProperty(CrnkProperties.RESOURCE_DEFAULT_DOMAIN);
		String webPathPrefix = getWebPathPrefix();
		if (resourceDefaultDomain != null) {
			String serviceUrl = buildServiceUrl(resourceDefaultDomain, webPathPrefix);
			moduleRegistry.getHttpRequestContextProvider().setServiceUrlProvider(new ConstantServiceUrlProvider(serviceUrl));
		}
	}

	public HttpRequestDispatcherImpl getRequestDispatcher() {
		PreconditionUtil.verify(requestDispatcher != null, "requestDispatcher not yet available, initialize CrnkBoot first");
		return requestDispatcher;
	}

	public ResourceRegistry getResourceRegistry() {
		PreconditionUtil.verify(resourceRegistry != null, "resourceRegistry not yet available, initialize CrnkBoot first");
		return resourceRegistry;
	}

	public ObjectMapper getObjectMapper() {
		if (objectMapper == null) {
			objectMapper = new ObjectMapper();
		}
		return objectMapper;
	}

	public void setObjectMapper(ObjectMapper objectMapper) {
		checkNotConfiguredYet();
		PreconditionUtil.verify(this.objectMapper == null, "ObjectMapper already set");
		this.objectMapper = objectMapper;
	}

	public PropertiesProvider getPropertiesProvider() {
		return propertiesProvider;
	}

	public void setPropertiesProvider(PropertiesProvider propertiesProvider) {
		checkNotConfiguredYet();
		LOGGER.debug("set properties provider {}", propertiesProvider);
		this.propertiesProvider = propertiesProvider;
	}

	/**
	 * @deprecated use {@link #setServiceUrlProvider(ServiceUrlProvider)}
	 */
	@Deprecated
	public ServiceUrlProvider getDefaultServiceUrlProvider() {
		return getServiceUrlProvider();
	}

	/**
	 * @deprecated use {@link #getServiceUrlProvider()}
	 */
	@Deprecated
	public void setDefaultServiceUrlProvider(ServiceUrlProvider defaultServiceUrlProvider) {
		setServiceUrlProvider(defaultServiceUrlProvider);
	}

	/**
	 * @return prefix like /api where to provide the JSON API endpoint. Always starts with a leading slash, but no trailing slash. Or null if not specified.
	 */
	public String getWebPathPrefix() {
		String pathPrefix = null;
		if (webPathPrefix != null) {
			pathPrefix = webPathPrefix;
		}else {
			pathPrefix = propertiesProvider.getProperty(CrnkProperties.WEB_PATH_PREFIX);
		}
		if(pathPrefix != null && !pathPrefix.startsWith("/")){
			pathPrefix = "/" + pathPrefix;
		}
		if(pathPrefix != null && pathPrefix.endsWith("/")){
			pathPrefix = pathPrefix.substring(0, pathPrefix.length() - 1);
		}
		return pathPrefix;
	}

	public ServiceDiscovery getServiceDiscovery() {
		return moduleRegistry.getServiceDiscovery();
	}

	public void setServiceDiscovery(ServiceDiscovery serviceDiscovery) {
		LOGGER.debug("set service discovery {}", serviceDiscovery);
		PreconditionUtil.verify(this.serviceDiscovery == null, "serviceDiscovery already set: %s", this.serviceDiscovery);
		this.serviceDiscovery = serviceDiscovery;
		moduleRegistry.setServiceDiscovery(serviceDiscovery);
	}

	/**
	 * Sets the default page limit for requests that return a collection of elements. If the api user does not
	 * specify the page limit, then this default value will be used.
	 * <p>
	 * This is important to prevent denial of service attacks on the server.
	 * <p>
	 * NOTE: This using this feature requires a {@link DefaultQuerySpecUrlMapper} and it does not work with the
	 * deprecated {@link QueryParamsBuilder}.
	 */
	public void setDefaultPageLimit(Long defaultPageLimit) {
		PreconditionUtil.verify(queryParamsBuilder == null,
				"Setting the default page limit requires using the QuerySpecDeserializer, but " +
						"it is null. Are you using QueryParams instead?");
		this.defaultPageLimit = defaultPageLimit;
	}

	/**
	 * Sets the maximum page limit allowed for paginated requests.
	 * <p>
	 * This is important to prevent denial of service attacks on the server.
	 * <p>
	 * NOTE: This using this feature requires a {@link DefaultQuerySpecUrlMapper} and it does not work with the
	 * deprecated {@link QueryParamsBuilder}.
	 */
	public void setMaxPageLimit(Long maxPageLimit) {
		PreconditionUtil
				.verify(queryParamsBuilder == null, "Setting the max page limit requires using the QuerySpecDeserializer, but " +
						"it is null. Are you using QueryParams instead?");
		this.maxPageLimit = maxPageLimit;
	}

	/**
	 * Sets the allow unknown attributes for API requests.
	 * <p>
	 * NOTE: Recommend to follow JSON API standards, but this feature can be used for custom implementations.
	 */
	public void setAllowUnknownAttributes() {
		PreconditionUtil
				.verify(queryParamsBuilder == null, "Allow unknown attributes requires using the QuerySpecDeserializer, but " +
						"it is null.");

		this.allowUnknownAttributes = true;
	}

	/**
	 * Sets the allow unknown query parameters for API requests.
	 * <p>
	 */
	public void setAllowUnknownParameters() {
		PreconditionUtil
				.verify(queryParamsBuilder == null, "Allow unknown parameters requires using the QuerySpecDeserializer, but " +
						"it is null.");

		this.allowUnknownParameters = true;
	}

	public ModuleRegistry getModuleRegistry() {
		return moduleRegistry;
	}

	/**
	 * @deprecated use {@link #getUrlMapper()}
	 */
	@Deprecated
	public QuerySpecDeserializer getQuerySpecDeserializer() {
		QuerySpecUrlMapper urlMapper = getUrlMapper();
		if (urlMapper instanceof UrlMapperAdapter) {
			return ((UrlMapperAdapter) urlMapper).getDeserializer();
		}
		return (QuerySpecDeserializer) urlMapper;
	}


	private void setupQuerySpecUrlMapper() {
		if (moduleRegistry.getUrlMapper() == null) {
			setupServiceDiscovery();

			List<QuerySpecUrlMapper> list = serviceDiscovery.getInstancesByType(QuerySpecUrlMapper.class);
			if (list.isEmpty()) {
				List<QuerySpecDeserializer> deserializers = serviceDiscovery.getInstancesByType(QuerySpecDeserializer.class);
				if (deserializers.isEmpty()) {
					moduleRegistry.setUrlMapper(new DefaultQuerySpecUrlMapper());
				}
				else {
					setQuerySpecDeserializerUnchecked(deserializers.get(0));
				}
			}
			else {
				moduleRegistry.setUrlMapper(list.get(0));
			}
		}

		QuerySpecUrlMapper urlMapper = moduleRegistry.getUrlMapper();
		if (urlMapper instanceof UnkonwnMappingAware) {
			if (allowUnknownAttributes == null) {
				String strAllow = propertiesProvider.getProperty(CrnkProperties.ALLOW_UNKNOWN_ATTRIBUTES);
				if (strAllow != null) {
					allowUnknownAttributes = Boolean.parseBoolean(strAllow);
				}
			}
			if (allowUnknownAttributes != null) {
				((UnkonwnMappingAware) urlMapper).setAllowUnknownAttributes(allowUnknownAttributes);
			}

			if (allowUnknownParameters == null) {
				String strAllow = propertiesProvider.getProperty(CrnkProperties.ALLOW_UNKNOWN_PARAMETERS);
				if (strAllow != null) {
					allowUnknownParameters = Boolean.parseBoolean(strAllow);
				}
			}
			if (allowUnknownParameters != null) {
				((UnkonwnMappingAware) urlMapper).setAllowUnknownParameters(allowUnknownParameters);
			}
		}
	}

	private void setupPagingBehavior() {
		setupServiceDiscovery();

		moduleRegistry.addAllPagingBehaviors(serviceDiscovery.getInstancesByType(PagingBehavior.class));

		if (!moduleRegistry.getPagingBehaviors().stream().anyMatch(it -> it instanceof OffsetLimitPagingBehavior)) {
			moduleRegistry.addPagingBehavior(new OffsetLimitPagingBehavior());
		}

		for (PagingBehavior pagingBehavior : moduleRegistry.getPagingBehaviors()) {
			if (pagingBehavior instanceof OffsetLimitPagingBehavior) {
				if (defaultPageLimit != null) {
					((OffsetLimitPagingBehavior) pagingBehavior).setDefaultLimit(defaultPageLimit);
				}
				else {
					LOGGER.warn(
							"no defaultLimit for paging specified, may lead to denial of service for in proper requests with "
									+ "large data sets"
					);
				}
				if (maxPageLimit != null) {
					((OffsetLimitPagingBehavior) pagingBehavior).setMaxPageLimit(maxPageLimit);
				}
			}
		}
	}

	/**
	 * Set the {@link QuerySpecDeserializer} to use to parse and handle query parameters.
	 * When invoked, overwrites previous {@link QueryParamsBuilder}s and QuerySpecDeserializers.
	 * <p>
	 * use {@link #setUrlMapper(QuerySpecUrlMapper)}}
	 */
	@Deprecated
	public void setQuerySpecDeserializer(QuerySpecDeserializer querySpecDeserializer) {
		checkNotConfiguredYet();
		setQuerySpecDeserializerUnchecked(querySpecDeserializer);
	}

	private void setQuerySpecDeserializerUnchecked(QuerySpecDeserializer querySpecDeserializer) {
		PreconditionUtil.verify(querySpecDeserializer != null, "A query spec deserializer must be provided, but is null");
		moduleRegistry.setUrlMapper(new UrlMapperAdapter(querySpecDeserializer));
		this.queryParamsBuilder = null;
	}

	/**
	 * Set the {@link QuerySpecUrlMapper} to use to parse and handle query parameters.
	 * When invoked, overwrites previous {@link QueryParamsBuilder}s and QuerySpecDeserializers.
	 */
	public void setUrlMapper(QuerySpecUrlMapper urlMapper) {
		checkNotConfiguredYet();
		PreconditionUtil.verify(urlMapper != null, "urlMapper parameter must not be null");
		moduleRegistry.setUrlMapper(urlMapper);
		this.queryParamsBuilder = null;
	}

	public boolean isNullDataResponseEnabled() {
		return Boolean.parseBoolean(propertiesProvider.getProperty(CrnkProperties.NULL_DATA_RESPONSE_ENABLED));
	}

	public ServiceUrlProvider getServiceUrlProvider() {
		return moduleRegistry.getHttpRequestContextProvider().getServiceUrlProvider();
	}

	public List<PagingBehavior> getPagingBehaviors() {
		return moduleRegistry.getPagingBehaviors();
	}

	public ControllerRegistry getControllerRegistry() {
		return controllerRegistry;
	}

	public QueryAdapterBuilder getQueryAdapterBuilder() {
		return queryAdapterBuilder;
	}

	public CoreModule getCoreModule() {
		return coreModule;
	}

	public QuerySpecUrlMapper getUrlMapper() {
		setupQuerySpecUrlMapper();
		return moduleRegistry.getUrlMapper();
	}

	public void setWebPathPrefix(String webPathPrefix) {
		this.webPathPrefix = webPathPrefix;
	}
}
