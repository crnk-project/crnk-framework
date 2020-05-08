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
import io.crnk.core.engine.error.ExceptionMapper;
import io.crnk.core.engine.filter.DocumentFilter;
import io.crnk.core.engine.filter.ResourceFilter;
import io.crnk.core.engine.filter.ResourceFilterDirectory;
import io.crnk.core.engine.filter.ResourceModificationFilter;
import io.crnk.core.engine.http.HttpRequestContextAware;
import io.crnk.core.engine.http.HttpStatusBehavior;
import io.crnk.core.engine.information.contributor.ResourceFieldContributor;
import io.crnk.core.engine.information.resource.ResourceInformationProviderModule;
import io.crnk.core.engine.internal.CoreModule;
import io.crnk.core.engine.internal.dispatcher.ControllerRegistry;
import io.crnk.core.engine.internal.dispatcher.controller.CollectionGetController;
import io.crnk.core.engine.internal.dispatcher.controller.Controller;
import io.crnk.core.engine.internal.dispatcher.controller.ControllerContext;
import io.crnk.core.engine.internal.dispatcher.controller.FieldResourceGetController;
import io.crnk.core.engine.internal.dispatcher.controller.FieldResourcePost;
import io.crnk.core.engine.internal.dispatcher.controller.RelationsDeleteController;
import io.crnk.core.engine.internal.dispatcher.controller.RelationshipsPatchController;
import io.crnk.core.engine.internal.dispatcher.controller.RelationshipsPostController;
import io.crnk.core.engine.internal.dispatcher.controller.RelationshipsResourceGetController;
import io.crnk.core.engine.internal.dispatcher.controller.ResourceDeleteController;
import io.crnk.core.engine.internal.dispatcher.controller.ResourceGetController;
import io.crnk.core.engine.internal.dispatcher.controller.ResourcePatchController;
import io.crnk.core.engine.internal.dispatcher.controller.ResourcePostController;
import io.crnk.core.engine.internal.document.mapper.DocumentMapper;
import io.crnk.core.engine.internal.exception.ExceptionMapperRegistry;
import io.crnk.core.engine.internal.http.HttpRequestDispatcherImpl;
import io.crnk.core.engine.internal.http.JsonApiRequestProcessor;
import io.crnk.core.engine.internal.jackson.JacksonModule;
import io.crnk.core.engine.internal.registry.ResourceRegistryImpl;
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
import io.crnk.core.engine.security.SecurityProvider;
import io.crnk.core.engine.url.ConstantServiceUrlProvider;
import io.crnk.core.engine.url.ServiceUrlProvider;
import io.crnk.core.module.Module;
import io.crnk.core.module.ModuleRegistry;
import io.crnk.core.module.SimpleModule;
import io.crnk.core.module.discovery.DefaultServiceDiscoveryFactory;
import io.crnk.core.module.discovery.EmptyServiceDiscovery;
import io.crnk.core.module.discovery.ServiceDiscovery;
import io.crnk.core.module.discovery.ServiceDiscoveryFactory;
import io.crnk.core.queryspec.internal.QuerySpecAdapterBuilder;
import io.crnk.core.queryspec.mapper.DefaultQuerySpecUrlMapper;
import io.crnk.core.queryspec.mapper.QuerySpecUrlMapper;
import io.crnk.core.queryspec.mapper.UnkonwnMappingAware;
import io.crnk.core.queryspec.mapper.UrlBuilder;
import io.crnk.core.queryspec.pagingspec.LimitBoundedPagingBehavior;
import io.crnk.core.queryspec.pagingspec.OffsetLimitPagingBehavior;
import io.crnk.core.queryspec.pagingspec.PagingBehavior;
import io.crnk.core.repository.Repository;
import io.crnk.core.repository.decorate.RepositoryDecoratorFactory;
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

	private boolean configured;

	private ResourceRegistry resourceRegistry;

	private HttpRequestDispatcherImpl requestDispatcher;

	private PropertiesProvider propertiesProvider = new NullPropertiesProvider();

	private ServiceDiscoveryFactory serviceDiscoveryFactory = new DefaultServiceDiscoveryFactory();

	private ServiceDiscovery serviceDiscovery = null;

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
		bootDiscovery();

		LOGGER.debug("completed setup");
	}

	private void bootDiscovery() {
		setupObjectMapper();

		resourceRegistry = new ResourceRegistryImpl(null, moduleRegistry);

		addModules();

		setupPagingBehavior();
		setupComponents();
		ResourceRegistryPart rootPart = setupResourceRegistry();

		moduleRegistry.init(objectMapper);

		setupRepositories(rootPart);

		requestDispatcher = createRequestDispatcher(moduleRegistry.getExceptionMapperRegistry());
		moduleRegistry.setDocumentMapper(documentMapper);

		logInfo();
	}

	private void setupServiceDiscovery() {
		if (serviceDiscovery == null) {
			if (serviceDiscoveryFactory != null) {
				setServiceDiscovery(serviceDiscoveryFactory.getInstance());
			}
			else {
				setServiceDiscovery(new EmptyServiceDiscovery());
			}
		}
	}

	private void logInfo() {
		int numResources = resourceRegistry.getEntries().size();
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
                ResourceRegistryPart defaultResourceRegistryPart = new DefaultResourceRegistryPart();
                hierarchialPart.putPart("", defaultResourceRegistryPart);
				moduleRegistry.getContext().addRegistryPart("", defaultResourceRegistryPart);
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
		return new QuerySpecAdapterBuilder(moduleRegistry.getUrlMapper(), moduleRegistry);
	}

	protected DocumentMapper createDocumentMapper() {
		ResourceFilterDirectory filterDirectory = moduleRegistry.getContext().getResourceFilterDirectory();
		ResultFactory resultFactory = moduleRegistry.getContext().getResultFactory();
		return new DocumentMapper(resourceRegistry, objectMapper, propertiesProvider, filterDirectory, resultFactory,
				serverInfo, moduleRegistry.getUrlBuilder());
	}

	protected ControllerRegistry createControllerRegistry() {
		Set<Controller> controllers = new HashSet<>();
		controllers.add(new RelationsDeleteController());
		controllers.add(new RelationshipsPatchController());
		controllers.add(new RelationshipsPostController());
		controllers.add(new ResourceDeleteController());
		controllers.add(new CollectionGetController());
		controllers.add(new FieldResourceGetController());
		controllers.add(new RelationshipsResourceGetController());
		controllers.add(new ResourceGetController());
		controllers.add(new FieldResourcePost());
		controllers.add(new ResourcePatchController());
		controllers.add(new ResourcePostController());

		ControllerContext context = new ControllerContext(moduleRegistry, this::getDocumentMapper);
		for (Controller controller : controllers) {
			controller.init(context);
		}
		ControllerRegistry controllerRegistry = new ControllerRegistry(controllers);
		moduleRegistry.setControllerRegistry(controllerRegistry);
		return controllerRegistry;
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
				moduleRegistry.setQueryAdapterBuilder(queryAdapterBuilder);

				this.addHttpRequestProcessor(new JsonApiRequestProcessor(context));
				super.setupModule(context);
			}
		};

		for (ExceptionMapper<?> exceptionMapper : getInstancesByType(ExceptionMapper.class)) {
			module.addExceptionMapper(exceptionMapper);
		}
		for (DocumentFilter filter : getInstancesByType(DocumentFilter.class)) {
			module.addFilter(filter);
		}
		List<Repository> repositories = getInstancesByType(Repository.class);
		for (Object repository : repositories) {
			module.addRepository(repository);
		}
		List<ResourceFieldContributor> resourceFieldContributors = getInstancesByType(ResourceFieldContributor.class);
		for (ResourceFieldContributor resourceFieldContributor : resourceFieldContributors) {
			module.addResourceFieldContributor(resourceFieldContributor);
		}
		List<RepositoryDecoratorFactory> decoratorFactories = getInstancesByType(RepositoryDecoratorFactory.class);
		for (RepositoryDecoratorFactory decoratorFactory : decoratorFactories) {
			module.addRepositoryDecoratorFactory(decoratorFactory);
		}
		List<ResourceModificationFilter> modificationFilters = getInstancesByType(ResourceModificationFilter.class);
		for (ResourceModificationFilter modificationFilter : modificationFilters) {
			module.addResourceModificationFilter(modificationFilter);
		}
		List<ResourceFilter> accessFilters = getInstancesByType(ResourceFilter.class);
		for (ResourceFilter accessFilter : accessFilters) {
			module.addResourceFilter(accessFilter);
		}
		List<HttpStatusBehavior> httpStatusBehaviors = getInstancesByType(HttpStatusBehavior.class);
		for (HttpStatusBehavior httpStatusBehavior : httpStatusBehaviors) {
			module.addHttpStatusBehavior(httpStatusBehavior);
		}
		List<SecurityProvider> securityProviders = getInstancesByType(SecurityProvider.class);
		for (SecurityProvider securityProvider : securityProviders) {
			module.addSecurityProvider(securityProvider);
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

	public UrlBuilder getUrlBuilder(){
		return moduleRegistry.getUrlBuilder();
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
	 * @return prefix like /api where to provide the JSON API endpoint. Always starts with a leading slash, but no trailing
	 * slash. Or null if not specified.
	 */
	public String getWebPathPrefix() {
		String pathPrefix = null;
		if (webPathPrefix != null) {
			pathPrefix = webPathPrefix;
		}
		else {
			pathPrefix = propertiesProvider.getProperty(CrnkProperties.WEB_PATH_PREFIX);
		}
		if (pathPrefix != null && !pathPrefix.startsWith("/")) {
			pathPrefix = "/" + pathPrefix;
		}
		if (pathPrefix != null && pathPrefix.endsWith("/")) {
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
	 */
	public void setDefaultPageLimit(Long defaultPageLimit) {
		this.defaultPageLimit = defaultPageLimit;
	}

	/**
	 * Sets the maximum page limit allowed for paginated requests.
	 * <p>
	 * This is important to prevent denial of service attacks on the server.
	 * <p>
	 */
	public void setMaxPageLimit(Long maxPageLimit) {
		this.maxPageLimit = maxPageLimit;
	}

	/**
	 * Sets the allow unknown attributes for API requests.
	 * <p>
	 * NOTE: Recommend to follow JSON API standards, but this feature can be used for custom implementations.
	 */
	public void setAllowUnknownAttributes() {
		this.allowUnknownAttributes = true;
	}

	/**
	 * Sets the allow unknown query parameters for API requests.
	 * <p>
	 */
	public void setAllowUnknownParameters() {
		this.allowUnknownParameters = true;
	}

	public ModuleRegistry getModuleRegistry() {
		return moduleRegistry;
	}

	private void setupQuerySpecUrlMapper() {
		if (moduleRegistry.getUrlMapper() == null) {
			List<QuerySpecUrlMapper> list = serviceDiscovery.getInstancesByType(QuerySpecUrlMapper.class);
			if (list.isEmpty()) {
				moduleRegistry.setUrlMapper(new DefaultQuerySpecUrlMapper());
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
		moduleRegistry.addAllPagingBehaviors(serviceDiscovery.getInstancesByType(PagingBehavior.class));

		if (moduleRegistry.getPagingBehaviors().isEmpty()) {
			moduleRegistry.addPagingBehavior(new OffsetLimitPagingBehavior());
		}

		for (PagingBehavior pagingBehavior : moduleRegistry.getPagingBehaviors()) {
			if (pagingBehavior instanceof LimitBoundedPagingBehavior) {
				if (defaultPageLimit != null) {
					((LimitBoundedPagingBehavior) pagingBehavior).setDefaultLimit(defaultPageLimit);
				}
				else {
					LOGGER.warn(
							"no defaultLimit for paging specified, may lead to denial of service for in proper requests with "
									+ "large data sets"
					);
				}
				if (maxPageLimit != null) {
					((LimitBoundedPagingBehavior) pagingBehavior).setMaxPageLimit(maxPageLimit);
				}
			}
		}
	}

	/**
	 * Set the {@link QuerySpecUrlMapper} to use to parse and handle query parameters.
	 * When invoked, overwrites previous QuerySpecDeserializers.
	 */
	public void setUrlMapper(QuerySpecUrlMapper urlMapper) {
		checkNotConfiguredYet();
		PreconditionUtil.verify(urlMapper != null, "urlMapper parameter must not be null");
		moduleRegistry.setUrlMapper(urlMapper);
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
