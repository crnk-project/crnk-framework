package io.crnk.core.boot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.crnk.core.engine.error.JsonApiExceptionMapper;
import io.crnk.core.engine.filter.DocumentFilter;
import io.crnk.core.engine.http.HttpRequestContextAware;
import io.crnk.core.engine.internal.CoreModule;
import io.crnk.core.engine.internal.dispatcher.ControllerRegistry;
import io.crnk.core.engine.internal.dispatcher.ControllerRegistryBuilder;
import io.crnk.core.engine.internal.document.mapper.DocumentMapper;
import io.crnk.core.engine.internal.exception.ExceptionMapperRegistry;
import io.crnk.core.engine.internal.http.HttpRequestProcessorImpl;
import io.crnk.core.engine.internal.http.JsonApiRequestProcessor;
import io.crnk.core.engine.internal.jackson.JacksonModule;
import io.crnk.core.engine.internal.registry.ResourceRegistryImpl;
import io.crnk.core.engine.internal.utils.ClassUtils;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.engine.properties.NullPropertiesProvider;
import io.crnk.core.engine.properties.PropertiesProvider;
import io.crnk.core.engine.query.QueryAdapterBuilder;
import io.crnk.core.engine.registry.*;
import io.crnk.core.engine.url.ConstantServiceUrlProvider;
import io.crnk.core.engine.url.ServiceUrlProvider;
import io.crnk.core.module.Module;
import io.crnk.core.module.ModuleRegistry;
import io.crnk.core.module.SimpleModule;
import io.crnk.core.module.discovery.DefaultServiceDiscoveryFactory;
import io.crnk.core.module.discovery.FallbackServiceDiscoveryFactory;
import io.crnk.core.module.discovery.ServiceDiscovery;
import io.crnk.core.module.discovery.ServiceDiscoveryFactory;
import io.crnk.core.queryspec.DefaultQuerySpecDeserializer;
import io.crnk.core.queryspec.QuerySpecDeserializer;
import io.crnk.core.queryspec.internal.QuerySpecAdapterBuilder;
import io.crnk.core.repository.RelationshipRepositoryV2;
import io.crnk.core.repository.Repository;
import io.crnk.core.repository.ResourceRepositoryV2;
import io.crnk.legacy.internal.QueryParamsAdapterBuilder;
import io.crnk.legacy.locator.JsonServiceLocator;
import io.crnk.legacy.locator.SampleJsonServiceLocator;
import io.crnk.legacy.queryParams.QueryParamsBuilder;
import io.crnk.legacy.repository.RelationshipRepository;
import io.crnk.legacy.repository.ResourceRepository;
import io.crnk.legacy.repository.annotations.JsonApiRelationshipRepository;
import io.crnk.legacy.repository.annotations.JsonApiResourceRepository;
import net.jodah.typetools.TypeResolver;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Facilitates the startup of Crnk in various environments (Spring, CDI,
 * JAX-RS, etc.).
 */
@SuppressWarnings("deprecation")
public class CrnkBoot {

	private final ModuleRegistry moduleRegistry = new ModuleRegistry();

	private ObjectMapper objectMapper;

	private QueryParamsBuilder queryParamsBuilder;

	private QuerySpecDeserializer querySpecDeserializer = new DefaultQuerySpecDeserializer();

	private boolean configured;

	private JsonServiceLocator serviceLocator = new SampleJsonServiceLocator();

	private ResourceRegistry resourceRegistry;

	private HttpRequestProcessorImpl requestDispatcher;

	private PropertiesProvider propertiesProvider = new NullPropertiesProvider();

	private ServiceDiscoveryFactory serviceDiscoveryFactory = new DefaultServiceDiscoveryFactory();

	private ServiceDiscovery serviceDiscovery;

	private DocumentMapper documentMapper;

	private List<Module> registeredModules = new ArrayList<>();

	private static String buildServiceUrl(String resourceDefaultDomain, String webPathPrefix) {
		return resourceDefaultDomain + (webPathPrefix != null ? webPathPrefix : "");
	}

	public void setServiceDiscoveryFactory(ServiceDiscoveryFactory factory) {
		checkNotConfiguredYet();
		this.serviceDiscoveryFactory = factory;
	}

	/**
	 * Set the {@link QueryParamsBuilder} to use to parse and handle query parameters.
	 * When invoked, overwrites previous QueryParamsBuilders and {@link QuerySpecDeserializer}s.
	 */
	public void setQueryParamsBuilds(QueryParamsBuilder queryParamsBuilder) {
		checkNotConfiguredYet();
		PreconditionUtil.assertNotNull("A query params builder must be provided, but is null", queryParamsBuilder);
		this.queryParamsBuilder = queryParamsBuilder;
		this.querySpecDeserializer = null;
	}

	/**
	 * Sets a JsonServiceLocator. No longer necessary if a ServiceDiscovery
	 * implementation is in place.
	 *
	 * @param serviceLocator
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
	 *
	 * @param serviceUrlProvider
	 */
	public void setServiceUrlProvider(ServiceUrlProvider serviceUrlProvider) {
		checkNotConfiguredYet();
		this.moduleRegistry.getHttpRequestContextProvider().setServiceUrlProvider(serviceUrlProvider);
	}

	private void checkNotConfiguredYet() {
		if (configured) {
			throw new IllegalStateException("cannot further modify CrnkFeature once configured/initialized by JAX-RS");
		}
	}

	/**
	 * Performs the setup.
	 */
	public void boot() {
		checkNotConfiguredYet();
		configured = true;

		setupServiceUrlProvider();
		setupServiceDiscovery();
		bootDiscovery();
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
		addModules();

		setupComponents();
		ResourceRegistryPart rootPart = setupResourceRegistry();

		moduleRegistry.init(objectMapper);

		setupRepositories(rootPart);

		requestDispatcher = createRequestDispatcher(moduleRegistry.getExceptionMapperRegistry());
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
		} else {
			HierarchicalResourceRegistryPart hierarchialPart = new HierarchicalResourceRegistryPart();
			for (Map.Entry<String, ResourceRegistryPart> entry : registryParts.entrySet()) {
				hierarchialPart.putPart(entry.getKey(), entry.getValue());
			}
			if (!registryParts.containsKey("")) {
				moduleRegistry.getContext().addRegistryPart("", new DefaultResourceRegistryPart());
			}
			rootPart = hierarchialPart;
		}

		resourceRegistry = new ResourceRegistryImpl(rootPart, moduleRegistry);
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

	private HttpRequestProcessorImpl createRequestDispatcher(ExceptionMapperRegistry exceptionMapperRegistry) {
		ControllerRegistryBuilder controllerRegistryBuilder =
				new ControllerRegistryBuilder(resourceRegistry, moduleRegistry.getTypeParser(), objectMapper,
						propertiesProvider, moduleRegistry.getContext().getResourceFilterDirectory());
		ControllerRegistry controllerRegistry = controllerRegistryBuilder.build();
		this.documentMapper = controllerRegistryBuilder.getDocumentMapper();

		QueryAdapterBuilder queryAdapterBuilder;
		if (queryParamsBuilder != null) {
			queryAdapterBuilder = new QueryParamsAdapterBuilder(queryParamsBuilder, moduleRegistry);
		} else {
			queryAdapterBuilder = new QuerySpecAdapterBuilder(querySpecDeserializer, moduleRegistry);
		}

		return new HttpRequestProcessorImpl(moduleRegistry, controllerRegistry, exceptionMapperRegistry, queryAdapterBuilder);
	}

	public DocumentMapper getDocumentMapper() {
		return documentMapper;
	}

	private void setupComponents() {
		// not that the provided default implementation here are added last and
		// as a consequence,
		// can be overriden by other modules, like the
		// JaxrsResourceRepositoryInformationBuilder.
		SimpleModule module = new SimpleModule("discovery") {

			@Override
			public void setupModule(ModuleContext context) {
				this.addHttpRequestProcessor(new JsonApiRequestProcessor(context));
				super.setupModule(context);
			}
		};

		for (JsonApiExceptionMapper<?> exceptionMapper : getInstancesByType(JsonApiExceptionMapper.class)) {
			module.addExceptionMapper(exceptionMapper);
		}
		for (DocumentFilter filter : getInstancesByType(DocumentFilter.class)) {
			module.addFilter(filter);
		}
		for (Object repository : getInstancesByType(Repository.class)) {
			setupRepository(module, repository);
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
		moduleRegistry.addModule(new CoreModule());
		moduleRegistry.setPropertiesProvider(propertiesProvider);
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

	private void setupRepository(SimpleModule module, Object repository) {
		if (repository instanceof ResourceRepository) {
			ResourceRepository resRepository = (ResourceRepository) repository;
			Class<?>[] typeArgs = TypeResolver.resolveRawArguments(ResourceRepository.class, resRepository.getClass());
			Class resourceClass = typeArgs[0];
			module.addRepository(resourceClass, resRepository);
		} else if (repository instanceof RelationshipRepository) {
			RelationshipRepository relRepository = (RelationshipRepository) repository;
			Class<?>[] typeArgs = TypeResolver.resolveRawArguments(RelationshipRepository.class, relRepository.getClass());
			Class sourceResourceClass = typeArgs[0];
			Class targetResourceClass = typeArgs[2];
			module.addRepository(sourceResourceClass, targetResourceClass, relRepository);
		} else if (repository instanceof ResourceRepositoryV2) {
			ResourceRepositoryV2<?, ?> resRepository = (ResourceRepositoryV2<?, ?>) repository;
			module.addRepository(resRepository.getResourceClass(), resRepository);
		} else if (repository instanceof RelationshipRepositoryV2) {
			RelationshipRepositoryV2<?, ?, ?, ?> relRepository = (RelationshipRepositoryV2<?, ?, ?, ?>) repository;
			module.addRepository(relRepository.getSourceResourceClass(), relRepository.getTargetResourceClass(), relRepository);
		} else {
			throw new IllegalStateException(repository.toString());
		}
	}

	private void addModules() {

		for (Module module : registeredModules) {
			moduleRegistry.addModule(module);
		}
		moduleRegistry.addModule(new JacksonModule(objectMapper));

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

	public HttpRequestProcessorImpl getRequestDispatcher() {
		PreconditionUtil.assertNotNull("expected requestDispatcher", requestDispatcher);
		return requestDispatcher;
	}

	public ResourceRegistry getResourceRegistry() {
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
		PreconditionUtil.assertNull("ObjectMapper already set", this.objectMapper);
		this.objectMapper = objectMapper;
	}

	public PropertiesProvider getPropertiesProvider() {
		return propertiesProvider;
	}

	public void setPropertiesProvider(PropertiesProvider propertiesProvider) {
		checkNotConfiguredYet();
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

	public String getWebPathPrefix() {
		return propertiesProvider.getProperty(CrnkProperties.WEB_PATH_PREFIX);
	}

	public ServiceDiscovery getServiceDiscovery() {
		return moduleRegistry.getServiceDiscovery();
	}

	public void setServiceDiscovery(ServiceDiscovery serviceDiscovery) {
		this.serviceDiscovery = serviceDiscovery;
		moduleRegistry.setServiceDiscovery(serviceDiscovery);
	}

	/**
	 * Sets the default page limit for requests that return a collection of elements. If the api user does not
	 * specify the page limit, then this default value will be used.
	 * <p>
	 * This is important to prevent denial of service attacks on the server.
	 * <p>
	 * NOTE: This using this feature requires a {@link QuerySpecDeserializer} and it does not work with the
	 * deprecated {@link QueryParamsBuilder}.
	 */
	public void setDefaultPageLimit(Long defaultPageLimit) {
		PreconditionUtil.assertNotNull("Setting the default page limit requires using the QuerySpecDeserializer, but " +
				"it is null. Are you using QueryParams instead?", this.querySpecDeserializer);
		((DefaultQuerySpecDeserializer) this.querySpecDeserializer).setDefaultLimit(defaultPageLimit);
	}

	/**
	 * Sets the maximum page limit allowed for paginated requests.
	 * <p>
	 * This is important to prevent denial of service attacks on the server.
	 * <p>
	 * NOTE: This using this feature requires a {@link QuerySpecDeserializer} and it does not work with the
	 * deprecated {@link QueryParamsBuilder}.
	 */
	public void setMaxPageLimit(Long maxPageLimit) {
		PreconditionUtil.assertNotNull("Setting the max page limit requires using the QuerySpecDeserializer, but " +
				"it is null. Are you using QueryParams instead?", this.querySpecDeserializer);
		((DefaultQuerySpecDeserializer) this.querySpecDeserializer).setMaxPageLimit(maxPageLimit);
	}

	public ModuleRegistry getModuleRegistry() {
		return moduleRegistry;
	}

	public QuerySpecDeserializer getQuerySpecDeserializer() {
		return querySpecDeserializer;
	}

	/**
	 * Set the {@link QuerySpecDeserializer} to use to parse and handle query parameters.
	 * When invoked, overwrites previous {@link QueryParamsBuilder}s and QuerySpecDeserializers.
	 */
	public void setQuerySpecDeserializer(QuerySpecDeserializer querySpecDeserializer) {
		checkNotConfiguredYet();
		PreconditionUtil.assertNotNull("A query spec deserializer must be provided, but is null", querySpecDeserializer);
		this.querySpecDeserializer = querySpecDeserializer;
		this.queryParamsBuilder = null;
	}

	public boolean isNullDataResponseEnabled() {
		return Boolean.parseBoolean(propertiesProvider.getProperty(CrnkProperties.NULL_DATA_RESPONSE_ENABLED));
	}

	public ServiceUrlProvider getServiceUrlProvider() {
		return moduleRegistry.getHttpRequestContextProvider().getServiceUrlProvider();
	}
}
