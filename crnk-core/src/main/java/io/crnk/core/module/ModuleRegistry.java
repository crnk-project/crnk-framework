package io.crnk.core.module;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.core.engine.dispatcher.RequestDispatcher;
import io.crnk.core.engine.error.ExceptionMapper;
import io.crnk.core.engine.error.JsonApiExceptionMapper;
import io.crnk.core.engine.filter.DocumentFilter;
import io.crnk.core.engine.filter.RepositoryFilter;
import io.crnk.core.engine.http.HttpRequestProcessor;
import io.crnk.core.engine.information.repository.*;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.information.resource.ResourceInformationBuilder;
import io.crnk.core.engine.information.resource.ResourceInformationBuilderContext;
import io.crnk.core.engine.internal.exception.ExceptionMapperLookup;
import io.crnk.core.engine.internal.exception.ExceptionMapperRegistry;
import io.crnk.core.engine.internal.exception.ExceptionMapperRegistryBuilder;
import io.crnk.core.engine.internal.registry.DefaultRegistryEntryBuilder;
import io.crnk.core.engine.internal.utils.ClassUtils;
import io.crnk.core.engine.internal.utils.Decorator;
import io.crnk.core.engine.internal.utils.MultivaluedMap;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.engine.parser.TypeParser;
import io.crnk.core.engine.properties.NullPropertiesProvider;
import io.crnk.core.engine.properties.PropertiesProvider;
import io.crnk.core.engine.registry.*;
import io.crnk.core.engine.security.SecurityProvider;
import io.crnk.core.module.Module.ModuleContext;
import io.crnk.core.module.discovery.MultiResourceLookup;
import io.crnk.core.module.discovery.ResourceLookup;
import io.crnk.core.module.discovery.ServiceDiscovery;
import io.crnk.core.module.internal.DefaultRepositoryInformationBuilderContext;
import io.crnk.core.repository.RelationshipRepositoryV2;
import io.crnk.core.repository.ResourceRepositoryV2;
import io.crnk.core.repository.decorate.RelationshipRepositoryDecorator;
import io.crnk.core.repository.decorate.RepositoryDecoratorFactory;
import io.crnk.core.repository.decorate.ResourceRepositoryDecorator;
import io.crnk.legacy.internal.DirectResponseRelationshipEntry;
import io.crnk.legacy.internal.DirectResponseResourceEntry;
import io.crnk.legacy.registry.AnnotatedRelationshipEntryBuilder;
import io.crnk.legacy.registry.AnnotatedResourceEntry;
import io.crnk.legacy.registry.DefaultResourceInformationBuilderContext;
import io.crnk.legacy.registry.RepositoryInstanceBuilder;
import io.crnk.legacy.repository.annotations.JsonApiRelationshipRepository;
import io.crnk.legacy.repository.annotations.JsonApiResourceRepository;

import java.util.*;

/**
 * Container for setting up and holding {@link Module} instances;
 */
public class ModuleRegistry {

	private TypeParser typeParser = new TypeParser();

	private ObjectMapper objectMapper;

	private ResourceRegistry resourceRegistry;

	private List<Module> modules = new ArrayList<>();

	private SimpleModule aggregatedModule = new SimpleModule(null);

	private volatile boolean initialized;

	private ServiceDiscovery serviceDiscovery;

	private boolean isServer = true;

	private ExceptionMapperRegistry exceptionMapperRegistry;

	private RequestDispatcher requestDispatcher;

	private PropertiesProvider propertiesProvider = new NullPropertiesProvider();

	public ModuleRegistry() {
		this(true);
	}

	public ModuleRegistry(boolean isServer) {
		this.isServer = isServer;
	}

	/**
	 * Register an new module to this registry and setup the module.
	 *
	 * @param module module
	 */
	public void addModule(Module module) {
		module.setupModule(new ModuleContextImpl());
		modules.add(module);
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
	 * Ensures the {@link ModuleRegistry#init(ObjectMapper, ResourceRegistry)}
	 * has not yet been called.
	 */
	protected void checkNotInitialized() {
		PreconditionUtil.verify(!initialized, "already initialized, cannot be changed anymore");
	}

	/**
	 * Returns a {@link ResourceInformationBuilder} instance that combines all
	 * instances registered by modules.
	 *
	 * @return resource information builder
	 */
	public ResourceInformationBuilder getResourceInformationBuilder() {
		CombinedResourceInformationBuilder resourceInformationBuilder =
				new CombinedResourceInformationBuilder(aggregatedModule.getResourceInformationBuilders());
		DefaultResourceInformationBuilderContext context =
				new DefaultResourceInformationBuilderContext(resourceInformationBuilder, typeParser);
		resourceInformationBuilder.init(context);
		return resourceInformationBuilder;
	}

	/**
	 * Returns a {@link RepositoryInformationBuilder} instance that combines all
	 * instances registered by modules.
	 *
	 * @return repository information builder
	 */
	public RepositoryInformationBuilder getRepositoryInformationBuilder() {
		return new CombinedRepositoryInformationBuilder(aggregatedModule.getRepositoryInformationBuilders());
	}

	/**
	 * Returns a {@link ResourceLookup} instance that combines all instances
	 * registered by modules.
	 *
	 * @return resource lookup
	 */
	public ResourceLookup getResourceLookup() {
		return new MultiResourceLookup(aggregatedModule.getResourceLookups());
	}

	public List<HttpRequestProcessor> getHttpRequestProcessors() {
		return aggregatedModule.getHttpRequestProcessors();
	}

	/**
	 * Returns a {@link SecurityProvider} instance that combines all instances
	 * registered by modules.
	 *
	 * @return resource lookup
	 */
	public SecurityProvider getSecurityProvider() {
		List<SecurityProvider> securityProviders = aggregatedModule.getSecurityProviders();
		PreconditionUtil.assertEquals("exactly one security provide must be installed, got: " + securityProviders, 1,
				securityProviders.size());
		return securityProviders.get(0);
	}

	/**
	 * Returns a {@link SecurityProvider} instance that combines all instances
	 * registered by modules.
	 *
	 * @return resource lookup
	 */
	public ServiceDiscovery getServiceDiscovery() {
		PreconditionUtil.assertNotNull("serviceDiscovery not yet available", serviceDiscovery);
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
		PreconditionUtil.assertFalse("already initialized", initialized);
		this.initialized = true;
		this.objectMapper = objectMapper;
		this.objectMapper.registerModules(getJacksonModules());

		applyRepositoryRegistrations(resourceRegistry);

		for (Module module : modules) {
			if (module instanceof InitializingModule) {
				((InitializingModule) module).init();
			}
		}

		ExceptionMapperLookup exceptionMapperLookup = getExceptionMapperLookup();
		ExceptionMapperRegistryBuilder mapperRegistryBuilder = new ExceptionMapperRegistryBuilder();
		exceptionMapperRegistry = mapperRegistryBuilder.build(exceptionMapperLookup);
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private void applyRepositoryRegistrations(ResourceRegistry resourceRegistry) {
		List<Object> repositories = aggregatedModule.getRepositories();


		MultivaluedMap<String, RepositoryInformation> typeInfoMapping = new MultivaluedMap<>();
		Map<Object, Object> infoRepositoryMapping = new HashMap<>();
		mapRepositoryRegistrations(repositories, typeInfoMapping, infoRepositoryMapping);

		for (String resourceType : typeInfoMapping.keySet()) {
			applyRepositoryRegistration(resourceType, typeInfoMapping, infoRepositoryMapping);
		}
	}

	private void applyRepositoryRegistration(String resourceType,
											 MultivaluedMap<String, RepositoryInformation> typeInfoMapping, Map<Object, Object> infoRepositoryMapping) {

		ResourceInformation resourceInformation = null;

		ResourceRepositoryInformation resourceRepositoryInformation = null;
		List<ResponseRelationshipEntry> relationshipEntries = new ArrayList<>();
		ResourceEntry resourceEntry = null;
		Class resourceClass = null;
		List<RepositoryInformation> repositoryInformations = typeInfoMapping.getList(resourceType);
		for (RepositoryInformation repositoryInformation : repositoryInformations) {
			if (repositoryInformation instanceof ResourceRepositoryInformation) {
				resourceRepositoryInformation = (ResourceRepositoryInformation) repositoryInformation;
				Object repository = infoRepositoryMapping.get(resourceRepositoryInformation);
				resourceEntry = setupResourceRepository(repository);
			} else {
				RelationshipRepositoryInformation relationshipRepositoryInformation =
						(RelationshipRepositoryInformation) repositoryInformation;
				Object repository = infoRepositoryMapping.get(repositoryInformation);
				setupRelationship(relationshipEntries, relationshipRepositoryInformation, repository);

				if (resourceClass == null) {
					resourceClass = relationshipRepositoryInformation.getSourceResourceClass().get();
				}
			}
		}

		if (resourceRepositoryInformation != null) {
			resourceInformation = resourceRepositoryInformation.getResourceInformation().get();
		}

		if (resourceInformation == null) {
			ResourceInformationBuilder resourceInformationBuilder = getResourceInformationBuilder();
			PreconditionUtil.assertNotNull("resource class cannot be determined", resourceClass);
			resourceInformation = resourceInformationBuilder.build(resourceClass);
		}

		RegistryEntry registryEntry = new RegistryEntry(resourceInformation, resourceRepositoryInformation, resourceEntry, relationshipEntries);
		registryEntry.initialize(this);
		resourceRegistry.addEntry(registryEntry);
	}

	private void mapRepositoryRegistrations(List<Object> repositories, MultivaluedMap<String, RepositoryInformation> resourceTypeMap,
											Map<Object, Object> resourceInformationMap) {

		RepositoryInformationBuilder repositoryInformationBuilder = getRepositoryInformationBuilder();
		RepositoryInformationBuilderContext builderContext = new DefaultRepositoryInformationBuilderContext(this);

		for (Object repository : repositories) {
			if (!(repository instanceof ResourceRepositoryDecorator)
					&& !(repository instanceof RelationshipRepositoryDecorator)) {
				RepositoryInformation repositoryInformation = repositoryInformationBuilder.build(repository, builderContext);
				if (repositoryInformation instanceof ResourceRepositoryInformation) {
					ResourceRepositoryInformation info = (ResourceRepositoryInformation) repositoryInformation;
					resourceInformationMap.put(info, repository);
					resourceTypeMap.add(info.getResourceType(), repositoryInformation);
				} else {
					RelationshipRepositoryInformation info = (RelationshipRepositoryInformation) repositoryInformation;
					resourceInformationMap.put(info, repository);
					resourceTypeMap.add(info.getSourceResourceType(), repositoryInformation);
				}
			}
		}

	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private ResourceEntry setupResourceRepository(Object repository) {
		final Object decoratedRepository = decorateRepository(repository);
		RepositoryInstanceBuilder repositoryInstanceBuilder = new RepositoryInstanceBuilder(null, repository.getClass()) {

			@Override
			public Object buildRepository() {
				return decoratedRepository;
			}
		};

		if (ClassUtils.getAnnotation(decoratedRepository.getClass(), JsonApiResourceRepository.class).isPresent()) {
			return new AnnotatedResourceEntry(repositoryInstanceBuilder);
		} else {
			return new DirectResponseResourceEntry(repositoryInstanceBuilder);
		}
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	public Object decorateRepository(Object repository) {
		Object decoratedRepository = repository;
		List<RepositoryDecoratorFactory> repositoryDecorators = getRepositoryDecoratorFactories();
		for (RepositoryDecoratorFactory repositoryDecorator : repositoryDecorators) {
			Decorator decorator = null;
			if (decoratedRepository instanceof RelationshipRepositoryV2) {
				decorator = repositoryDecorator.decorateRepository((RelationshipRepositoryV2) decoratedRepository);
			} else if (decoratedRepository instanceof ResourceRepositoryV2) {
				decorator = repositoryDecorator.decorateRepository((ResourceRepositoryV2) decoratedRepository);
			}
			if (decorator != null) {
				decorator.setDecoratedObject(decoratedRepository);
				decoratedRepository = decorator;
			}
		}
		if (decoratedRepository instanceof ResourceRegistryAware) {
			((ResourceRegistryAware) decoratedRepository).setResourceRegistry(resourceRegistry);
		}
		return decoratedRepository;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private void setupRelationship(List<ResponseRelationshipEntry> relationshipEntries,
								   final RelationshipRepositoryInformation relationshipRepositoryInformation, final Object relRepository) {

		final Object decoratedRepository = decorateRepository(relRepository);
		RepositoryInstanceBuilder<Object> relationshipInstanceBuilder = new RepositoryInstanceBuilder<Object>(null, (Class) relRepository.getClass()) {

			@Override
			public Object buildRepository() {
				return decoratedRepository;
			}
		};

		final String targetResourceType = relationshipRepositoryInformation.getTargetResourceType();
		if (ClassUtils.getAnnotation(relRepository.getClass(), JsonApiRelationshipRepository.class).isPresent()) {
			relationshipEntries.add(new AnnotatedRelationshipEntryBuilder(this, relationshipInstanceBuilder));
		} else {
			ResponseRelationshipEntry relationshipEntry = new DirectResponseRelationshipEntry(relationshipInstanceBuilder) {

				@Override
				public String getTargetResourceType() {
					return targetResourceType;
				}
			};
			relationshipEntries.add(relationshipEntry);
		}
	}

	/**
	 * @return {@link DocumentFilter} added by all modules
	 */
	public List<DocumentFilter> getFilters() {
		return aggregatedModule.getFilters();
	}

	/**
	 * @return {@link RepositoryFilter} added by all modules
	 */
	public List<RepositoryFilter> getRepositoryFilters() {
		return aggregatedModule.getRepositoryFilters();
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

	public <T extends Module> io.crnk.core.utils.Optional<T> getModule(Class<T> clazz) {
		for (Module module : modules) {
			if (clazz.isInstance(module)) {
				return io.crnk.core.utils.Optional.of((T) module);
			}
		}
		return io.crnk.core.utils.Optional.empty();
	}

	public TypeParser getTypeParser() {
		return typeParser;
	}

	public ModuleContext getContext() {
		return new ModuleContextImpl();
	}

	public ExceptionMapperRegistry getExceptionMapperRegistry() {
		PreconditionUtil.assertNotNull("exceptionMapperRegistry not set", exceptionMapperRegistry);
		return exceptionMapperRegistry;
	}

	public Map<String, ResourceRegistryPart> getRegistryParts() {
		return aggregatedModule.getRegistryParts();
	}

	public List<RegistryEntry> getRegistryEntries() {
		return aggregatedModule.getRegistryEntries();
	}

	/**
	 * Combines all {@link ResourceInformationBuilder} instances provided by the
	 * registered {@link Module}.
	 */
	static class CombinedResourceInformationBuilder implements ResourceInformationBuilder {

		private Collection<ResourceInformationBuilder> builders;

		public CombinedResourceInformationBuilder(List<ResourceInformationBuilder> builders) {
			this.builders = builders;
		}

		@Override
		public boolean accept(Class<?> resourceClass) {
			for (ResourceInformationBuilder builder : builders) {
				if (builder.accept(resourceClass)) {
					return true;
				}
			}
			return false;
		}

		@Override
		public ResourceInformation build(Class<?> resourceClass) {
			for (ResourceInformationBuilder builder : builders) {
				if (builder.accept(resourceClass)) {
					return builder.build(resourceClass);
				}
			}
			throw new UnsupportedOperationException(
					"no ResourceInformationBuilder available for " + resourceClass.getName());
		}

		@Override
		public String getResourceType(Class<?> resourceClass) {
			for (ResourceInformationBuilder builder : builders) {
				if (builder.accept(resourceClass)) {
					return builder.getResourceType(resourceClass);
				}
			}
			return null;
		}

		@Override
		public void init(ResourceInformationBuilderContext context) {
			for (ResourceInformationBuilder builder : builders) {
				builder.init(context);
			}
		}
	}

	/**
	 * Combines all {@link RepositoryInformationBuilder} instances provided by
	 * the registered {@link Module}.
	 */
	static class CombinedRepositoryInformationBuilder implements RepositoryInformationBuilder {

		private Collection<RepositoryInformationBuilder> builders;

		public CombinedRepositoryInformationBuilder(List<RepositoryInformationBuilder> builders) {
			this.builders = builders;
		}

		@Override
		public boolean accept(Object repository) {
			for (RepositoryInformationBuilder builder : builders) {
				if (builder.accept(repository)) {
					return true;
				}
			}
			return false;
		}

		@Override
		public RepositoryInformation build(Object repository, RepositoryInformationBuilderContext context) {
			for (RepositoryInformationBuilder builder : builders) {
				if (builder.accept(repository)) {
					return builder.build(repository, context);
				}
			}
			throw new UnsupportedOperationException(
					"no RepositoryInformationBuilder available for " + repository.getClass().getName());
		}

		@Override
		public boolean accept(Class<?> repositoryClass) {
			for (RepositoryInformationBuilder builder : builders) {
				if (builder.accept(repositoryClass)) {
					return true;
				}
			}
			return false;
		}

		@Override
		public RepositoryInformation build(Class<?> repositoryClass, RepositoryInformationBuilderContext context) {
			for (RepositoryInformationBuilder builder : builders) {
				if (builder.accept(repositoryClass)) {
					return builder.build(repositoryClass, context);
				}
			}
			throw new UnsupportedOperationException(
					"no RepositoryInformationBuilder available for " + repositoryClass.getName());
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
		public Set<JsonApiExceptionMapper> getExceptionMappers() {
			Set<JsonApiExceptionMapper> set = new HashSet<JsonApiExceptionMapper>();
			for (ExceptionMapperLookup lookup : lookups) {
				set.addAll(lookup.getExceptionMappers());
			}
			return set;
		}
	}

	class ModuleContextImpl implements Module.ModuleContext {

		@Override
		public void addResourceInformationBuilder(ResourceInformationBuilder resourceInformationBuilder) {
			aggregatedModule.addResourceInformationBuilder(resourceInformationBuilder);
		}

		@Override
		public void addRepositoryInformationBuilder(RepositoryInformationBuilder repositoryInformationBuilder) {
			aggregatedModule.addRepositoryInformationBuilder(repositoryInformationBuilder);
		}

		@Override
		public void addResourceLookup(ResourceLookup resourceLookup) {
			aggregatedModule.addResourceLookup(resourceLookup);
		}

		@Override
		public void addJacksonModule(com.fasterxml.jackson.databind.Module module) {
			checkNotInitialized();
			aggregatedModule.addJacksonModule(module);
		}

		@Override
		public ResourceRegistry getResourceRegistry() {
			if (resourceRegistry == null) {
				throw new IllegalStateException("resourceRegistry not yet available");
			}
			return resourceRegistry;
		}

		@Override
		public void addFilter(DocumentFilter filter) {
			aggregatedModule.addFilter(filter);
		}

		@Override
		public void addExceptionMapperLookup(ExceptionMapperLookup exceptionMapperLookup) {
			aggregatedModule.addExceptionMapperLookup(exceptionMapperLookup);
		}

		@Override
		public void addExceptionMapper(ExceptionMapper<?> exceptionMapper) {
			aggregatedModule.addExceptionMapper(exceptionMapper);
		}

		@Override
		public void addRepository(Class<?> type, Object repository) {
			aggregatedModule.addRepository(repository);
		}

		@Override
		public void addRepository(Class<?> sourceType, Class<?> targetType, Object repository) {
			aggregatedModule.addRepository(repository);
		}

		@Override
		public void addSecurityProvider(SecurityProvider securityProvider) {
			aggregatedModule.addSecurityProvider(securityProvider);
		}

		@Override
		public SecurityProvider getSecurityProvider() {
			return ModuleRegistry.this.getSecurityProvider();
		}

		@Override
		public void addHttpRequestProcessor(HttpRequestProcessor processor) {
			ModuleRegistry.this.aggregatedModule.addHttpRequestProcessor(processor);
		}

		@Override
		public ObjectMapper getObjectMapper() {
			return ModuleRegistry.this.objectMapper;
		}

		@Override
		public void addRegistryPart(String prefix, ResourceRegistryPart part) {
			aggregatedModule.addRegistryPart(prefix, part);
		}

		@Override
		public ServiceDiscovery getServiceDiscovery() {
			return ModuleRegistry.this.getServiceDiscovery();
		}

		@Override
		public void addRepositoryFilter(RepositoryFilter filter) {
			aggregatedModule.addRepositoryFilter(filter);
		}

		@Override
		public void addRepositoryDecoratorFactory(RepositoryDecoratorFactory decoratorFactory) {
			aggregatedModule.addRepositoryDecoratorFactory(decoratorFactory);
		}

		@Override
		public void addRepository(Object repository) {
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
		public ResourceInformationBuilder getResourceInformationBuilder() {
			return ModuleRegistry.this.getResourceInformationBuilder();
		}

		@Override
		public ExceptionMapperRegistry getExceptionMapperRegistry() {
			return ModuleRegistry.this.getExceptionMapperRegistry();
		}

		@Override
		public RequestDispatcher getRequestDispatcher() {
			return ModuleRegistry.this.requestDispatcher;
		}

		@Override
		public RegistryEntryBuilder newRegistryEntryBuilder() {
			return new DefaultRegistryEntryBuilder(ModuleRegistry.this);
		}

		@Override
		public void addRegistryEntry(RegistryEntry entry) {
			aggregatedModule.addRegistryEntry(entry);
		}
	}
}
