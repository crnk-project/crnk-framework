package io.crnk.core.module;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.core.engine.dispatcher.RequestDispatcher;
import io.crnk.core.engine.error.ExceptionMapper;
import io.crnk.core.engine.filter.DocumentFilter;
import io.crnk.core.engine.filter.RepositoryFilter;
import io.crnk.core.engine.filter.ResourceFilter;
import io.crnk.core.engine.filter.ResourceFilterDirectory;
import io.crnk.core.engine.filter.ResourceModificationFilter;
import io.crnk.core.engine.http.HttpRequestContextAware;
import io.crnk.core.engine.http.HttpRequestContextProvider;
import io.crnk.core.engine.http.HttpRequestProcessor;
import io.crnk.core.engine.http.HttpStatusBehavior;
import io.crnk.core.engine.information.InformationBuilder;
import io.crnk.core.engine.information.NamingStrategy;
import io.crnk.core.engine.information.contributor.ResourceFieldContributor;
import io.crnk.core.engine.information.repository.RepositoryInformation;
import io.crnk.core.engine.information.repository.RepositoryInformationProvider;
import io.crnk.core.engine.information.repository.RepositoryInformationProviderContext;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.information.resource.ResourceInformationProvider;
import io.crnk.core.engine.information.resource.ResourceInformationProviderContext;
import io.crnk.core.engine.internal.dispatcher.ControllerRegistry;
import io.crnk.core.engine.internal.document.mapper.DocumentMapper;
import io.crnk.core.engine.internal.document.mapper.DocumentMappingConfig;
import io.crnk.core.engine.internal.exception.ExceptionMapperLookup;
import io.crnk.core.engine.internal.exception.ExceptionMapperRegistry;
import io.crnk.core.engine.internal.exception.ExceptionMapperRegistryBuilder;
import io.crnk.core.engine.internal.information.DefaultInformationBuilder;
import io.crnk.core.engine.internal.information.resource.ResourceFieldImpl;
import io.crnk.core.engine.internal.registry.DefaultRegistryEntryBuilder;
import io.crnk.core.engine.internal.repository.RepositoryAdapterFactory;
import io.crnk.core.engine.internal.utils.JsonApiUrlBuilder;
import io.crnk.core.engine.internal.utils.MultivaluedMap;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.engine.parser.TypeParser;
import io.crnk.core.engine.properties.NullPropertiesProvider;
import io.crnk.core.engine.properties.PropertiesProvider;
import io.crnk.core.engine.query.QueryAdapterBuilder;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.RegistryEntryBuilder;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.engine.registry.ResourceRegistryPart;
import io.crnk.core.engine.result.ImmediateResultFactory;
import io.crnk.core.engine.result.ResultFactory;
import io.crnk.core.engine.security.SecurityProvider;
import io.crnk.core.engine.security.SecurityProviderContext;
import io.crnk.core.module.Module.ModuleContext;
import io.crnk.core.module.discovery.MultiResourceLookup;
import io.crnk.core.module.discovery.ResourceLookup;
import io.crnk.core.module.discovery.ServiceDiscovery;
import io.crnk.core.module.internal.DefaultRepositoryInformationProviderContext;
import io.crnk.core.module.internal.ModuleUtils;
import io.crnk.core.module.internal.ResourceFilterDirectoryImpl;
import io.crnk.core.queryspec.mapper.QuerySpecUrlContext;
import io.crnk.core.queryspec.mapper.QuerySpecUrlMapper;
import io.crnk.core.queryspec.mapper.UrlBuilder;
import io.crnk.core.queryspec.pagingspec.PagingBehavior;
import io.crnk.core.queryspec.pagingspec.PagingSpec;
import io.crnk.core.repository.decorate.RepositoryDecoratorFactory;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.core.utils.Prioritizable;
import io.crnk.legacy.registry.DefaultResourceInformationProviderContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Container for setting up and holding {@link Module} instances;
 */
public class ModuleRegistry {

	private static final Logger LOGGER = LoggerFactory.getLogger(ModuleRegistry.class);

	private ResultFactory resultFactory;

	private Map<String, String> serverInfo;

	private DocumentMapper documentMapper;

	private DocumentMappingConfig documentMappingConfig = DocumentMappingConfig.create();

	private JsonApiUrlBuilder urlBuilder = new JsonApiUrlBuilder(this);

	public QueryAdapterBuilder getQueryAdapterBuilder() {
		return queryAdapterBuilder;
	}

	@Deprecated
	public void setControllerRegistry(ControllerRegistry controllerRegistry) {
		this.controllerRegistry = controllerRegistry;
	}

	@Deprecated
	public void setQueryAdapterBuilder(QueryAdapterBuilder queryAdapterBuilder) {
		this.queryAdapterBuilder = queryAdapterBuilder;
	}

	public ControllerRegistry getControllerRegistry() {
		return controllerRegistry;
	}

	public ObjectMapper getObjectMapper() {
		return objectMapper;
	}

	public void setDocumentMapper(DocumentMapper documentMapper) {
		this.documentMapper = documentMapper;
	}

	public DocumentMappingConfig getDocumentMappingConfig() {
		return documentMappingConfig;
	}

	public List<NamingStrategy> getNamingStrategies() {
		return aggregatedModule.getNamingStrategies();
	}

	enum InitializedState {
		NOT_INITIALIZED,
		INITIALIZING,
		INITIALIZED
	}

	private TypeParser typeParser = new TypeParser();

	private ObjectMapper objectMapper;

	private ResourceRegistry resourceRegistry;

	private QuerySpecUrlMapper urlMapper;

	private List<Module> modules = new ArrayList<>();

	private SimpleModule aggregatedModule = new SimpleModule(null);

	private volatile InitializedState initializedState = InitializedState.NOT_INITIALIZED;

	private ResourceInformationProvider resourceInformationProvider;

	private ServiceDiscovery serviceDiscovery;

	private boolean isServer = true;

	private ExceptionMapperRegistry exceptionMapperRegistry;

	private RequestDispatcher requestDispatcher;

	private HttpRequestContextProvider httpRequestContextProvider = new HttpRequestContextProvider(() -> getResultFactory(), this);

	private PropertiesProvider propertiesProvider = new NullPropertiesProvider();

	private ResourceFilterDirectory filterBehaviorProvider;

	private ControllerRegistry controllerRegistry;

	private QueryAdapterBuilder queryAdapterBuilder;

	private Map<Object, RepositoryInformation> repositoryInformations = new ConcurrentHashMap<>();


	public ModuleRegistry() {
		this(true);
	}

	public ModuleRegistry(boolean isServer) {
		this.isServer = isServer;
	}

	public DefaultInformationBuilder getInformationBuilder() {
		return new DefaultInformationBuilder(typeParser);
	}

	public List<ResourceModificationFilter> getResourceModificationFilters() {
		return Prioritizable.prioritze(aggregatedModule.getResourceModificationFilters());
	}

	public void setServerInfo(Map<String, String> serverInfo) {
		this.serverInfo = serverInfo;
	}

	public Map<String, String> getServerInfo() {
		return serverInfo;
	}

	public ResultFactory getResultFactory() {
		if (resultFactory == null) {
			throw new IllegalStateException("resultFactory not yet available");
		}
		return resultFactory;
	}

	public Collection<Object> getRepositories() {
		return aggregatedModule.getRepositories();
	}

	public void setResultFactory(ResultFactory resultFactory) {
		if (this.resultFactory != null) {
			throw new IllegalStateException("already set to " + this.resultFactory);
		}
		this.resultFactory = resultFactory;
	}

	/**
	 * Register an new module to this registry and setup the module.
	 *
	 * @param module module
	 */
	public void addModule(Module module) {
		checkState(InitializedState.NOT_INITIALIZED, InitializedState.NOT_INITIALIZED);
		LOGGER.debug("adding module {}", module);
		module.setupModule(new ModuleContextImpl(module));
		modules.add(module);
	}

	public UrlBuilder getUrlBuilder() {
		return urlBuilder;
	}

	/**
	 * Add the given {@link PagingBehavior} to the module
	 *
	 * @param pagingBehavior the paging behavior
	 */
	public void addPagingBehavior(PagingBehavior pagingBehavior) {
		aggregatedModule.addPagingBehavior(pagingBehavior);
	}

	public void addAllPagingBehaviors(List<PagingBehavior> pagingBehaviors) {
		for (PagingBehavior pagingBehavior : pagingBehaviors) {
			this.aggregatedModule.addPagingBehavior(pagingBehavior);
		}
	}

	public List<PagingBehavior> getPagingBehaviors() {
		return this.aggregatedModule.getPagingBehaviors();
	}

	public List<ResourceFieldContributor> getResourceFieldContributors() {
		return aggregatedModule.getResourceFieldContributors();
	}

	public ResourceRegistry getResourceRegistry() {
		if (resourceRegistry == null) {
			throw new IllegalStateException("resourceRegistry not yet available");
		}
		return resourceRegistry;
	}

	public void setResourceRegistry(ResourceRegistry resourceRegistry) {
		this.resourceRegistry = resourceRegistry;
	}

	public void setRequestDispatcher(RequestDispatcher requestDispatcher) {
		this.requestDispatcher = requestDispatcher;
	}

	/**
	 * @return all Jackson modules registered by modules.
	 */
	public List<com.fasterxml.jackson.databind.Module> getJacksonModules() {
		return aggregatedModule.getJacksonModules();
	}

	/**
	 * Ensures the {@link ModuleRegistry#init(ObjectMapper)}
	 * has not yet been called.
	 */

	protected void checkState(InitializedState minState, InitializedState maxState) {
		PreconditionUtil.verify(initializedState.ordinal() >= minState.ordinal(), "not yet initialized, cannot yet be called");
		PreconditionUtil
				.verify(initializedState.ordinal() <= maxState.ordinal(), "already initialized, cannot be called anymore");
	}

	/**
	 * Returns a {@link ResourceInformationProvider} instance that combines all
	 * instances registered by modules.
	 *
	 * @return resource information builder
	 */
	public ResourceInformationProvider getResourceInformationBuilder() {
		if (resourceInformationProvider == null) {
			resourceInformationProvider =
					new CombinedResourceInformationProvider(
							Prioritizable.prioritze(aggregatedModule.getResourceInformationProviders()));
			InformationBuilder informationBuilder = new DefaultInformationBuilder(typeParser);
			DefaultResourceInformationProviderContext context =
					new DefaultResourceInformationProviderContext(resourceInformationProvider, informationBuilder, typeParser,
							() -> objectMapper);
			resourceInformationProvider.init(context);
		}
		return resourceInformationProvider;
	}

	/**
	 * Returns a {@link RepositoryInformationProvider} instance that combines all
	 * instances registered by modules.
	 *
	 * @return repository information builder
	 */
	public RepositoryInformationProvider getRepositoryInformationBuilder() {
		return new CombinedRepositoryInformationProvider(aggregatedModule.getRepositoryInformationProviders());
	}

	/**
	 * Returns a {@link ResourceLookup} instance that combines all instances
	 * registered by modules.
	 *
	 * @return resource lookup
	 */
	public ResourceLookup getResourceLookup() {
		checkState(InitializedState.INITIALIZING, InitializedState.INITIALIZED);
		return new MultiResourceLookup(aggregatedModule.getResourceLookups());
	}

	public List<HttpRequestProcessor> getHttpRequestProcessors() {
		checkState(InitializedState.INITIALIZED, InitializedState.INITIALIZED);
		return Prioritizable.prioritze(aggregatedModule.getHttpRequestProcessors());
	}

	/**
	 * Returns a {@link SecurityProvider} instance that combines all instances
	 * registered by modules.
	 *
	 * @return resource lookup
	 */
	public SecurityProvider getSecurityProvider() {
		checkState(InitializedState.INITIALIZED, InitializedState.INITIALIZED);
		List<SecurityProvider> securityProviders = aggregatedModule.getSecurityProviders();
		return new AggregatedSecurityProvider(securityProviders);
	}

	public List<SecurityProvider> getSecurityProviders() {
		return aggregatedModule.getSecurityProviders();
	}

	class AggregatedSecurityProvider implements SecurityProvider {

		private final List<SecurityProvider> securityProviders;

		public AggregatedSecurityProvider(List<SecurityProvider> securityProviders) {
			this.securityProviders = securityProviders;
		}

		@Override
		public boolean isUserInRole(String role, SecurityProviderContext context) {
			PreconditionUtil.verify(securityProviders.size() != 0, "no SecurityProvider installed to check permissions");
			for (SecurityProvider securityProvider : securityProviders) {
				if (securityProvider.isUserInRole(role, context)) {
					return true;
				}
			}
			return false;
		}

		@Override
		public boolean isAuthenticated(SecurityProviderContext context) {
			PreconditionUtil.verify(securityProviders.size() != 0, "no SecurityProvider installed to check permissions");
			for (SecurityProvider securityProvider : securityProviders) {
				if (securityProvider.isAuthenticated(context)) {
					return true;
				}
			}
			return false;
		}
	}

	/**
	 * Returns a {@link SecurityProvider} instance that combines all instances
	 * registered by modules.
	 *
	 * @return resource lookup
	 */
	public ServiceDiscovery getServiceDiscovery() {
		PreconditionUtil.verify(serviceDiscovery != null, "serviceDiscovery not yet available");
		return serviceDiscovery;
	}

	public void setServiceDiscovery(ServiceDiscovery serviceDiscovery) {
		this.serviceDiscovery = serviceDiscovery;
	}

	/**
	 * Returns a {@link PropertiesProvider} instance
	 *
	 * @return property provider
	 */
	public PropertiesProvider getPropertiesProvider() {
		return propertiesProvider;
	}

	public void setPropertiesProvider(PropertiesProvider propertiesProvider) {
		this.propertiesProvider = propertiesProvider;
	}

	/**
	 * Initializes the {@link ModuleRegistry} and applies all pending changes.
	 * After the initialization completed, it is not possible to add any further
	 * modules.
	 *
	 * @param objectMapper object mapper
	 */
	public void init(ObjectMapper objectMapper) {
		if (resultFactory == null) {
			resultFactory = new ImmediateResultFactory();
		}
		PreconditionUtil.verifyEquals(InitializedState.NOT_INITIALIZED, initializedState, "already initialized");
		this.initializedState = InitializedState.INITIALIZING;
		this.objectMapper = objectMapper;
		this.objectMapper.registerModules(getJacksonModules());
		typeParser.setObjectMapper(objectMapper);

		initializeModules();

		if (isServer) {
			applyRepositoryRegistrations();
			applyResourceRegistrations();
		}

		ExceptionMapperLookup exceptionMapperLookup = getExceptionMapperLookup();
		ExceptionMapperRegistryBuilder mapperRegistryBuilder = new ExceptionMapperRegistryBuilder();
		exceptionMapperRegistry = mapperRegistryBuilder.build(exceptionMapperLookup);

		filterBehaviorProvider =
				new ResourceFilterDirectoryImpl(aggregatedModule.getResourceFilters(), httpRequestContextProvider,
						resourceRegistry);
		this.initializedState = InitializedState.INITIALIZED;
	}

	private MultivaluedMap<Module, ModuleExtension> extensionMap = new MultivaluedMap<>();

	private void initializeModules() {
		setExtensions();

		HashSet<Module> initializedModules = new HashSet<>();
		for (Module module : modules) {
			initializeModule(module, initializedModules);
		}
	}

	private void setExtensions() {
		MultivaluedMap<Module, ModuleExtension> reverseExtensionMap = new MultivaluedMap<>();
		for (ModuleExtension extension : aggregatedModule.getExtensions()) {
			Optional<? extends Module> optModule = getModule(extension.getTargetModule());
			if (optModule.isPresent()) {
				reverseExtensionMap.add(optModule.get(), extension);
			} else if (!extension.isOptional()) {
				throw new IllegalStateException(extension.getTargetModule() + " not installed but required by " + extension);
			}
		}

		for (Module extendedModule : reverseExtensionMap.keySet()) {
			List<ModuleExtension> extensions = reverseExtensionMap.getList(extendedModule);
			PreconditionUtil
					.assertTrue("module must extend ModuleExtensionAware", extendedModule instanceof ModuleExtensionAware);
			((ModuleExtensionAware) extendedModule).setExtensions(extensions);
		}
	}

	private void initializeModule(Module module, HashSet<Module> initializedModules) {
		if (!initializedModules.contains(module)) {
			initializedModules.add(module);

			// init dependencies first
			if (extensionMap.containsKey(module)) {
				List<ModuleExtension> dependencies = extensionMap.getList(module);
				for (ModuleExtension dependencyExtension : dependencies) {
					Class<? extends Module> targetModule = dependencyExtension.getTargetModule();
					Optional<? extends Module> dependencyModule = getModule(targetModule);
					PreconditionUtil.verify(dependencyModule.isPresent() || dependencyExtension.isOptional(),
							"module dependency from %s to %s not available, use CrnkBoot.addModoule(...) or service discovery to"
									+ " add the later",
							module.getModuleName(), targetModule);
					if (dependencyModule.isPresent()) {
						initializeModule(dependencyModule.get(), initializedModules);
					}
				}
			}

			// initialize
			if (module instanceof InitializingModule) {
				((InitializingModule) module).init();
			}
		}
	}

	public HttpRequestContextProvider getHttpRequestContextProvider() {
		return httpRequestContextProvider;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private void applyRepositoryRegistrations() {
		Collection<Object> repositories = aggregatedModule.getRepositories();
		for (Object repository : repositories) {
			applyRepositoryRegistration(repository);
		}
	}

	protected Set<Class> findResourceClasses() {
		Set<Class> resourceClasses = new HashSet<>();
		resourceClasses.addAll(getResourceLookup().getResourceClasses());
		if (resourceRegistry != null) {
			resourceClasses
					.addAll(resourceRegistry.getEntries().stream().map(it -> it.getResourceInformation().getResourceClass())
							.collect(Collectors.toList()));
		}
		for (Class resourceClass : new ArrayList<>(resourceClasses)) {
			findChildResources(resourceClasses, resourceClass);
		}
		return resourceClasses;
	}

	private void applyResourceRegistrations() {
		Set<Class> resourceClasses = findResourceClasses();
		Map<Class, RegistryEntry> addtionalEntryMap = new HashMap<>();
		for (Class<?> resourceClass : resourceClasses) {
			if (resourceRegistry.getEntry(resourceClass) == null) {
				applyResourceRegistration(resourceClass, addtionalEntryMap);
			}
		}

		initOpposites(false);
		initNesting();
	}

	@Deprecated // not official API, to be moved at some point
	public void initOpposites(boolean allowMissing) {
		Collection<RegistryEntry> entries = resourceRegistry.getEntries();
		for (RegistryEntry entry : entries) {
			ResourceInformation resourceInformation = entry.getResourceInformation();
			for (ResourceField field : resourceInformation.getRelationshipFields()) {
				String oppositeName = field.getOppositeName();
				if (oppositeName != null) {
					if (allowMissing && !resourceRegistry.hasEntry(field.getOppositeResourceType())) {
						continue;
					}
					RegistryEntry oppositeEntry = resourceRegistry.getEntry(field.getOppositeResourceType());
					ResourceField oppositeField = oppositeEntry.getResourceInformation().findFieldByUnderlyingName(oppositeName);
					PreconditionUtil.verify(oppositeField != null, "unable to find opposite field '%s' for field %s", oppositeName, field);

					String oppositeOppositeName = oppositeField.getOppositeName();
					if (oppositeOppositeName != null) {
						PreconditionUtil.verifyEquals(field.getUnderlyingName(), oppositeOppositeName, "opposite references do not match for %s and %s", field, oppositeField);
					} else {
						((ResourceFieldImpl) oppositeField).setOppositeName(field.getUnderlyingName());
					}
				}
			}
		}
	}

	private void initNesting() {
		Collection<RegistryEntry> entries = resourceRegistry.getEntries();
		for (RegistryEntry entry : entries) {
			ResourceInformation resourceInformation = entry.getResourceInformation();
			if (resourceInformation.getIdField() != null) {
				resourceInformation.initNesting();
			}
		}
	}

	private void findChildResources(Set<Class> resourceClasses, Class clazz) {
		JsonApiResource annotation = (JsonApiResource) clazz.getDeclaredAnnotation(JsonApiResource.class);
		if (annotation != null) {
			Class[] subTypes = annotation.subTypes();
			for (Class subType : subTypes) {
				if (!resourceClasses.contains(subType)) {
					resourceClasses.add(subType);
					findChildResources(resourceClasses, subType);
				}
			}
		}
	}

	private RegistryEntry applyResourceRegistration(Class<?> resourceClass, Map<Class, RegistryEntry> additionalEntryMap) {
		if (additionalEntryMap.containsKey(resourceClass)) {
			return additionalEntryMap.get(resourceClass);
		}
		RegistryEntry entry = resourceRegistry.getEntry(resourceClass);
		if (entry == null) {
			Class<?> superclass = resourceClass.getSuperclass();
			RegistryEntry parentEntry;
			if (resourceInformationProvider.accept(superclass)) {
				parentEntry = applyResourceRegistration(superclass, additionalEntryMap);
			} else {
				throw new IllegalStateException("super type " + superclass + " of " + resourceClass
						+ " is not a resource. Is it annotated with @JsonApiResource?");
			}

			LOGGER.debug("adding resource {}", resourceClass);
			PreconditionUtil.verify(parentEntry != null,
					"unable to find repository for resource type %s, make sure a repository is backing this type or one of its "
							+ "super types",
					resourceClass);
			PreconditionUtil.verify(resourceInformationProvider.accept(resourceClass),
					"make sure resource type %s is a valid resource, e.g. annotated with @JsonApiResource", resourceClass);
			ResourceInformation information = resourceInformationProvider.build(resourceClass);
			ModuleUtils.adaptInformation(information, this);
			PreconditionUtil.verify(parentEntry.getResourceInformation().getResourcePath().equals(information.getResourcePath()),
					"resource type %s without repository implementation must specify a @JsonApiResource.resourcePath matching "
							+ "one of its parent repositories",
					resourceClass);

			RegistryEntryBuilder entryBuilder = getContext().newRegistryEntryBuilder();
			entryBuilder.resource().from(information);
			entry = entryBuilder.build();
			entry.setParentRegistryEntry(parentEntry);
			getContext().addRegistryEntry(entry);
			Class<?> parentClass = parentEntry.getResourceInformation().getResourceClass();
			PreconditionUtil
					.verify(resourceClass.getSuperclass().equals(parentClass), "%s must be a subType of %s", resourceClass,
							parentClass);
		}
		additionalEntryMap.put(resourceClass, entry);
		return entry;
	}

	private void applyRepositoryRegistration(Object repository) {
		if (repository instanceof HttpRequestContextAware) {
			((HttpRequestContextAware) repository).setHttpRequestContextProvider(getHttpRequestContextProvider());
		}

		RegistryEntryBuilder entryBuilder = getContext().newRegistryEntryBuilder();
		entryBuilder.fromImplementation(repository);
		RegistryEntry entry = entryBuilder.build();
		if (entry != null) {
			ModuleUtils.adaptInformation(entry.getResourceInformation(), this);
			resourceRegistry.addEntry(entry);
		}
	}

	/**
	 * @return {@link DocumentFilter} added by all modules
	 */
	public List<DocumentFilter> getFilters() {
		return Prioritizable.prioritze(aggregatedModule.getFilters());
	}


	/**
	 * @return {@link RepositoryFilter} added by all modules
	 */
	public List<RepositoryFilter> getRepositoryFilters() {
		return Prioritizable.prioritze(aggregatedModule.getRepositoryFilters());
	}

	/**
	 * @return {@link RepositoryDecoratorFactory} added by all modules
	 */
	public List<RepositoryDecoratorFactory> getRepositoryDecoratorFactories() {
		return aggregatedModule.getRepositoryDecoratorFactories();
	}

	/**
	 * @return combined {@link ExceptionMapperLookup} added by all modules
	 */
	public ExceptionMapperLookup getExceptionMapperLookup() {
		return new CombinedExceptionMapperLookup(aggregatedModule.getExceptionMapperLookups());
	}

	public List<Module> getModules() {
		return modules;
	}

	public <T extends Module> Optional<T> getModule(Class<T> clazz) {
		for (Module module : modules) {
			if (clazz.isInstance(module)) {
				return Optional.of((T) module);
			}
		}
		return Optional.empty();
	}

	public TypeParser getTypeParser() {
		return typeParser;
	}

	public RepositoryInformation getRepositoryInformation(Object repository) {
		RepositoryInformation repositoryInformation = repositoryInformations.get(repository);
		if (repositoryInformation == null) {
			RepositoryInformationProvider repositoryInformationBuilder = getRepositoryInformationBuilder();
			repositoryInformation = repositoryInformationBuilder.build(repository, new
					DefaultRepositoryInformationProviderContext(this));
			repositoryInformations.put(repository, repositoryInformation);
		}
		return repositoryInformation;
	}

	public ModuleContext getContext() {
		return new ModuleContextImpl(null);
	}

	public ExceptionMapperRegistry getExceptionMapperRegistry() {
		PreconditionUtil.verify(exceptionMapperRegistry != null,
				"exceptionMapperRegistry not yet available, wait for initialization to complete");
		return exceptionMapperRegistry;
	}

	public Map<String, ResourceRegistryPart> getRegistryParts() {
		return aggregatedModule.getRegistryParts();
	}

	public List<RegistryEntry> getRegistryEntries() {
		return aggregatedModule.getRegistryEntries();
	}

	public void setObjectMapper(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
		typeParser.setObjectMapper(objectMapper);
	}

	public List<RepositoryAdapterFactory> getRepositoryAdapterFactories() {
		return aggregatedModule.getRepositoryAdapterFactories();
	}

	/**
	 * Combines all {@link ResourceInformationProvider} instances provided by the
	 * registered {@link Module}.
	 */
	static class CombinedResourceInformationProvider implements ResourceInformationProvider {

		private Collection<ResourceInformationProvider> builders;

		public CombinedResourceInformationProvider(List<ResourceInformationProvider> builders) {
			this.builders = builders;
		}

		@Override
		public boolean accept(Class<?> resourceClass) {
			for (ResourceInformationProvider builder : builders) {
				if (builder.accept(resourceClass)) {
					return true;
				}
			}
			return false;
		}

		@Override
		public ResourceInformation build(Class<?> resourceClass) {
			for (ResourceInformationProvider builder : builders) {
				if (builder.accept(resourceClass)) {
					return builder.build(resourceClass);
				}
			}
			throw new UnsupportedOperationException(
					"no ResourceInformationProvider available for " + resourceClass.getName());
		}

		@Override
		public String getResourceType(Class<?> resourceClass) {
			for (ResourceInformationProvider builder : builders) {
				if (builder.accept(resourceClass)) {
					return builder.getResourceType(resourceClass);
				}
			}
			return null;
		}

		@Override
		public String getResourcePath(Class<?> resourceClass) {
			for (ResourceInformationProvider builder : builders) {
				if (builder.accept(resourceClass)) {
					return builder.getResourcePath(resourceClass);
				}
			}
			return null;
		}

		@Override
		public void init(ResourceInformationProviderContext context) {
			for (ResourceInformationProvider builder : builders) {
				builder.init(context);
			}
		}
	}

	/**
	 * Combines all {@link RepositoryInformationProvider} instances provided by
	 * the registered {@link Module}.
	 */
	static class CombinedRepositoryInformationProvider implements RepositoryInformationProvider {

		private Collection<RepositoryInformationProvider> builders;

		public CombinedRepositoryInformationProvider(List<RepositoryInformationProvider> builders) {
			this.builders = builders;
		}

		@Override
		public boolean accept(Object repository) {
			for (RepositoryInformationProvider builder : builders) {
				if (builder.accept(repository)) {
					return true;
				}
			}
			return false;
		}

		@Override
		public RepositoryInformation build(Object repository, RepositoryInformationProviderContext context) {
			for (RepositoryInformationProvider builder : builders) {
				if (builder.accept(repository)) {
					return builder.build(repository, context);
				}
			}
			throw new UnsupportedOperationException(
					"no RepositoryInformationProvider available for " + repository.getClass().getName());
		}

		@Override
		public boolean accept(Class<?> repositoryClass) {
			for (RepositoryInformationProvider builder : builders) {
				if (builder.accept(repositoryClass)) {
					return true;
				}
			}
			return false;
		}

		@Override
		public RepositoryInformation build(Class<?> repositoryClass, RepositoryInformationProviderContext context) {
			for (RepositoryInformationProvider builder : builders) {
				if (builder.accept(repositoryClass)) {
					return builder.build(repositoryClass, context);
				}
			}
			throw new UnsupportedOperationException(
					"no RepositoryInformationProvider available for " + repositoryClass.getName());
		}
	}

	/**
	 * Combines all {@link ExceptionMapperLookup} instances provided by the
	 * registered {@link Module}.
	 */
	static class CombinedExceptionMapperLookup implements ExceptionMapperLookup {

		private Collection<ExceptionMapperLookup> lookups;

		public CombinedExceptionMapperLookup(List<ExceptionMapperLookup> lookups) {
			this.lookups = lookups;
		}

		@SuppressWarnings("rawtypes")
		@Override
		public List<ExceptionMapper> getExceptionMappers() {
			List<ExceptionMapper> set = new ArrayList<>();
			for (ExceptionMapperLookup lookup : lookups) {
				set.addAll(lookup.getExceptionMappers());
			}
			return set;
		}
	}

	class ModuleContextImpl implements Module.ModuleContext {

		private final Module module;

		public ModuleContextImpl(Module module) {
			this.module = module;
		}

		@Override
		public void addPagingBehavior(PagingBehavior pagingBehavior) {
			LOGGER.debug("adding paging behavior {}", pagingBehavior);
			checkState(InitializedState.NOT_INITIALIZED, InitializedState.NOT_INITIALIZED);
			aggregatedModule.addPagingBehavior(pagingBehavior);
		}

		@Override
		public void addResourceInformationProvider(ResourceInformationProvider resourceInformationProvider) {
			LOGGER.debug("adding resource information provider {}", resourceInformationProvider);
			checkState(InitializedState.NOT_INITIALIZED, InitializedState.NOT_INITIALIZED);
			aggregatedModule.addResourceInformationProvider(resourceInformationProvider);
		}

		@Override
		public void addRepositoryInformationBuilder(RepositoryInformationProvider repositoryInformationProvider) {
			LOGGER.debug("adding repository information provider {}", repositoryInformationProvider);
			checkState(InitializedState.NOT_INITIALIZED, InitializedState.NOT_INITIALIZED);
			aggregatedModule.addRepositoryInformationBuilder(repositoryInformationProvider);
		}

		@Override
		public void addResourceLookup(ResourceLookup resourceLookup) {
			LOGGER.debug("adding resource lookup {}", resourceLookup);
			checkState(InitializedState.NOT_INITIALIZED, InitializedState.NOT_INITIALIZED);
			aggregatedModule.addResourceLookup(resourceLookup);
		}

		@Override
		public void addJacksonModule(com.fasterxml.jackson.databind.Module module) {
			LOGGER.debug("adding jackson module {}", module);
			checkState(InitializedState.NOT_INITIALIZED, InitializedState.NOT_INITIALIZED);
			aggregatedModule.addJacksonModule(module);
		}

		@Override
		public ResourceRegistry getResourceRegistry() {
			checkState(InitializedState.INITIALIZING, InitializedState.INITIALIZED);
			if (resourceRegistry == null) {
				throw new IllegalStateException("resourceRegistry not yet available");
			}
			return resourceRegistry;
		}

		@Override
		public void addFilter(DocumentFilter filter) {
			LOGGER.debug("adding document filter {}", filter);
			checkState(InitializedState.NOT_INITIALIZED, InitializedState.NOT_INITIALIZED);
			aggregatedModule.addFilter(filter);
		}

		@Override
		public void addExceptionMapperLookup(ExceptionMapperLookup exceptionMapperLookup) {
			checkState(InitializedState.NOT_INITIALIZED, InitializedState.NOT_INITIALIZED);
			aggregatedModule.addExceptionMapperLookup(exceptionMapperLookup);
		}

		@Override
		public void addExceptionMapper(ExceptionMapper<?> exceptionMapper) {
			LOGGER.debug("adding exception mapper {}", exceptionMapper);
			checkState(InitializedState.NOT_INITIALIZED, InitializedState.NOT_INITIALIZED);
			aggregatedModule.addExceptionMapper(exceptionMapper);
		}

		@Override
		public void addSecurityProvider(SecurityProvider securityProvider) {
			LOGGER.debug("adding security provider {}", securityProvider);
			checkState(InitializedState.NOT_INITIALIZED, InitializedState.NOT_INITIALIZED);
			aggregatedModule.addSecurityProvider(securityProvider);
		}

		@Override
		public SecurityProvider getSecurityProvider() {
			checkState(InitializedState.INITIALIZING, InitializedState.INITIALIZED);
			return ModuleRegistry.this.getSecurityProvider();
		}

		@Override
		public DocumentMapper getDocumentMapper() {
			return documentMapper;
		}

		@Override
		public void setResultFactory(ResultFactory resultFactory) {
			ModuleRegistry.this.setResultFactory(resultFactory);
		}

		@Override
		public void addExtension(ModuleExtension extension) {
			LOGGER.debug("adding extension {}", extension);
			checkState(InitializedState.NOT_INITIALIZED, InitializedState.NOT_INITIALIZED);
			aggregatedModule.addExtension(extension);
			extensionMap.add(module, extension);
		}

		@Override
		public void addHttpRequestProcessor(HttpRequestProcessor processor) {
			LOGGER.debug("adding http request processor {}", processor);
			checkState(InitializedState.NOT_INITIALIZED, InitializedState.NOT_INITIALIZED);
			ModuleRegistry.this.aggregatedModule.addHttpRequestProcessor(processor);
		}

		@Override
		public ObjectMapper getObjectMapper() {
			PreconditionUtil.verify(objectMapper != null, "objectMapper not yet available before initialization");
			return ModuleRegistry.this.objectMapper;
		}

		@Override
		public void addRegistryPart(String prefix, ResourceRegistryPart part) {
			LOGGER.debug("adding registry part {}", part);
			checkState(InitializedState.NOT_INITIALIZED, InitializedState.NOT_INITIALIZED);
			aggregatedModule.addRegistryPart(prefix, part);
		}

		@Override
		public ServiceDiscovery getServiceDiscovery() {
			return ModuleRegistry.this.getServiceDiscovery();
		}

		@Override
		public void addRepositoryFilter(RepositoryFilter filter) {
			LOGGER.debug("adding repository filter {}", filter);
			checkState(InitializedState.NOT_INITIALIZED, InitializedState.NOT_INITIALIZED);
			aggregatedModule.addRepositoryFilter(filter);
		}

		@Override
		public void addResourceFilter(ResourceFilter filter) {
			LOGGER.debug("adding resource filter {}", filter);
			checkState(InitializedState.NOT_INITIALIZED, InitializedState.NOT_INITIALIZED);
			aggregatedModule.addResourceFilter(filter);
		}

		@Override
		public void addResourceFieldContributor(ResourceFieldContributor contributor) {
			LOGGER.debug("adding resource field contributor {}", contributor);
			checkState(InitializedState.NOT_INITIALIZED, InitializedState.NOT_INITIALIZED);
			aggregatedModule.addResourceFieldContributor(contributor);
		}

		@Override
		public void addRepositoryDecoratorFactory(RepositoryDecoratorFactory decoratorFactory) {
			LOGGER.debug("adding repository decorator factory {}", decoratorFactory);
			checkState(InitializedState.NOT_INITIALIZED, InitializedState.NOT_INITIALIZED);
			aggregatedModule.addRepositoryDecoratorFactory(decoratorFactory);
		}

		@Override
		public void addRepository(Object repository) {
			LOGGER.debug("adding repository {}", repository);
			checkState(InitializedState.NOT_INITIALIZED, InitializedState.INITIALIZING);
			aggregatedModule.addRepository(repository);
		}

		@Override
		public boolean isServer() {
			return isServer;
		}

		@Override
		public TypeParser getTypeParser() {
			return typeParser;
		}

		@Override
		public ResourceInformationProvider getResourceInformationBuilder() {
			checkState(InitializedState.INITIALIZING, InitializedState.INITIALIZED);
			return ModuleRegistry.this.getResourceInformationBuilder();
		}

		@Override
		public ExceptionMapperRegistry getExceptionMapperRegistry() {
			checkState(InitializedState.INITIALIZING, InitializedState.INITIALIZED);
			return ModuleRegistry.this.getExceptionMapperRegistry();
		}


		@Override
		public void addHttpStatusBehavior(HttpStatusBehavior httpStatusBehavior) {
			checkState(InitializedState.NOT_INITIALIZED, InitializedState.INITIALIZING);
			aggregatedModule.addHttpStatusBehavior(httpStatusBehavior);
		}

		@Override
		public List<NamingStrategy> getNamingStrategies() {
			return aggregatedModule.getNamingStrategies();
		}

		@Override
		public void addNamingStrategy(NamingStrategy namingStrategy) {
			aggregatedModule.addNamingStrategy(namingStrategy);
		}

		@Override
		public RequestDispatcher getRequestDispatcher() {
			checkState(InitializedState.INITIALIZING, InitializedState.INITIALIZED);
			return ModuleRegistry.this.requestDispatcher;
		}

		@Override
		public RegistryEntryBuilder newRegistryEntryBuilder() {
			return new DefaultRegistryEntryBuilder(ModuleRegistry.this);
		}

		@Override
		public void addRegistryEntry(RegistryEntry entry) {
			LOGGER.debug("adding registry entry {}", entry);
			checkState(InitializedState.NOT_INITIALIZED, InitializedState.INITIALIZING);
			aggregatedModule.addRegistryEntry(entry);
		}

		@Override
		public ResourceFilterDirectory getResourceFilterDirectory() {
			checkState(InitializedState.INITIALIZING, InitializedState.INITIALIZED);
			return filterBehaviorProvider;
		}

		@Override
		public void addResourceModificationFilter(ResourceModificationFilter filter) {
			LOGGER.debug("adding resource modification filter  {}", filter);
			aggregatedModule.addResourceModificationFilter(filter);
		}

		@Override
		public ResultFactory getResultFactory() {
			return resultFactory;
		}

		@Override
		public List<DocumentFilter> getDocumentFilters() {
			return ModuleRegistry.this.getFilters();
		}

		@Override
		public void addRepositoryAdapterFactory(RepositoryAdapterFactory repositoryAdapterFactory) {
			LOGGER.debug("adding repository adapter factory {}", repositoryAdapterFactory);
			aggregatedModule.addRepositoryAdapterFactory(repositoryAdapterFactory);
		}

		@Override
		public ModuleRegistry getModuleRegistry() {
			return ModuleRegistry.this;
		}

		@Override
		public PropertiesProvider getPropertiesProvider() {
			return propertiesProvider;
		}
	}


	public QuerySpecUrlMapper getUrlMapper() {
		return urlMapper;
	}

	public void setUrlMapper(QuerySpecUrlMapper urlMapper) {
		this.urlMapper = urlMapper;

		if (urlMapper != null) {
			this.urlMapper.init(new QuerySpecUrlContext() {

				@Override
				public ResourceRegistry getResourceRegistry() {
					return ModuleRegistry.this.getResourceRegistry();
				}

				@Override
				public TypeParser getTypeParser() {
					return ModuleRegistry.this.getTypeParser();
				}

				@Override
				public ObjectMapper getObjectMapper() {
					return ModuleRegistry.this.objectMapper;
				}

				@Override
				public UrlBuilder getUrlBuilder() {
					return ModuleRegistry.this.urlBuilder;
				}
			});
		}
	}

	public PagingBehavior findPagingBehavior(Class<? extends PagingSpec> pagingSpecType) {
		if (pagingSpecType == null) {
			return null;
		}
		List<PagingBehavior> pagingBehaviors = getPagingBehaviors();
		PreconditionUtil.verify(!pagingBehaviors.isEmpty(), "no paging behaviors installed");
		for (PagingBehavior pagingBehavior : pagingBehaviors) {
			if (pagingSpecType == PagingSpec.class || pagingBehavior.supports(pagingSpecType)) {
				return pagingBehavior;
			}
		}
		throw new IllegalStateException("no pagingBehavior implementation available for " + pagingSpecType);
	}

	public List<HttpStatusBehavior> getHttpStatusProviders() {
		return aggregatedModule.getHttpStatusBehaviors();
	}

	public HttpStatusBehavior getHttpStatusProvider() {
		List<HttpStatusBehavior> behaviors = Prioritizable.prioritze(aggregatedModule.getHttpStatusBehaviors());
		return context -> {
			for (HttpStatusBehavior behavior : behaviors) {
				Integer status = behavior.getStatus(context);
				if (status != null) {
					return status;
				}
			}
			throw new IllegalStateException("failed to compute HTTP status for " + context);
		};
	}


}
