package io.crnk.core.module;

import io.crnk.core.engine.error.JsonApiExceptionMapper;
import io.crnk.core.engine.filter.DocumentFilter;
import io.crnk.core.engine.filter.RepositoryFilter;
import io.crnk.core.engine.http.HttpRequestProcessor;
import io.crnk.core.engine.information.repository.RepositoryInformationBuilder;
import io.crnk.core.engine.information.resource.ResourceInformationBuilder;
import io.crnk.core.engine.internal.exception.ExceptionMapperLookup;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistryPart;
import io.crnk.core.engine.security.SecurityProvider;
import io.crnk.core.module.discovery.ResourceLookup;
import io.crnk.core.repository.decorate.RepositoryDecoratorFactory;

import java.util.*;

/**
 * Vanilla {@link Module} implementation that allows registration of extensions.
 */
public class SimpleModule implements Module {

	private List<ResourceInformationBuilder> resourceInformationBuilders = new ArrayList<>();

	private List<HttpRequestProcessor> httpRequestProcessors = new ArrayList<>();

	private List<RepositoryInformationBuilder> repositoryInformationBuilders = new ArrayList<>();

	private List<DocumentFilter> filters = new ArrayList<>();

	private List<RepositoryFilter> repositoryFilters = new ArrayList<>();

	private List<RepositoryDecoratorFactory> repositoryDecoratorFactories = new ArrayList<>();

	private List<SecurityProvider> securityProviders = new ArrayList<>();

	private List<ResourceLookup> resourceLookups = new ArrayList<>();

	private List<com.fasterxml.jackson.databind.Module> jacksonModules = new ArrayList<>();

	private List<Object> repositories = new ArrayList<>();

	private List<ExceptionMapperLookup> exceptionMapperLookups = new ArrayList<>();

	private List<RegistryEntry> registryEntries = new ArrayList<>();

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
		for (ResourceInformationBuilder resourceInformationBuilder : resourceInformationBuilders) {
			context.addResourceInformationBuilder(resourceInformationBuilder);
		}
		for (RepositoryInformationBuilder resourceInformationBuilder : repositoryInformationBuilders) {
			context.addRepositoryInformationBuilder(resourceInformationBuilder);
		}
		for (ResourceLookup resourceLookup : resourceLookups) {
			context.addResourceLookup(resourceLookup);
		}
		for (DocumentFilter filter : filters) {
			context.addFilter(filter);
		}
		for (RepositoryFilter filter : repositoryFilters) {
			context.addRepositoryFilter(filter);
		}
		for (RepositoryDecoratorFactory decorator : repositoryDecoratorFactories) {
			context.addRepositoryDecoratorFactory(decorator);
		}
		for (com.fasterxml.jackson.databind.Module jacksonModule : jacksonModules) {
			context.addJacksonModule(jacksonModule);
		}
		for (Object repository : repositories) {
			context.addRepository(repository);
		}
		for (ExceptionMapperLookup exceptionMapperLookup : exceptionMapperLookups) {
			context.addExceptionMapperLookup(exceptionMapperLookup);
		}
		for (HttpRequestProcessor httpRequestProcessor : httpRequestProcessors) {
			context.addHttpRequestProcessor(httpRequestProcessor);
		}
	}

	private void checkInitialized() {
		if (context != null) {
			throw new IllegalStateException("module cannot be changed addModule was called");
		}
	}

	/**
	 * Registers a new {@link ResourceInformationBuilder} with this module.
	 *
	 * @param resourceInformationBuilder resource information builder
	 */
	public void addResourceInformationBuilder(ResourceInformationBuilder resourceInformationBuilder) {
		checkInitialized();
		resourceInformationBuilders.add(resourceInformationBuilder);
	}

	/**
	 * Registers a new {@link RepositoryInformationBuilder} with this module.
	 *
	 * @param repositoryInformationBuilder repository information builder
	 */
	public void addRepositoryInformationBuilder(RepositoryInformationBuilder repositoryInformationBuilder) {
		checkInitialized();
		repositoryInformationBuilders.add(repositoryInformationBuilder);
	}

	public void addExceptionMapperLookup(ExceptionMapperLookup exceptionMapperLookup) {
		checkInitialized();
		exceptionMapperLookups.add(exceptionMapperLookup);
	}

	public void addExceptionMapper(@SuppressWarnings("rawtypes") JsonApiExceptionMapper exceptionMapper) {
		checkInitialized();
		ExceptionMapperLookup exceptionMapperLookup = new CollectionExceptionMapperLookup(exceptionMapper);
		exceptionMapperLookups.add(exceptionMapperLookup);
	}

	protected List<ResourceInformationBuilder> getResourceInformationBuilders() {
		checkInitialized();
		return Collections.unmodifiableList(resourceInformationBuilders);
	}

	protected List<RepositoryInformationBuilder> getRepositoryInformationBuilders() {
		checkInitialized();
		return Collections.unmodifiableList(repositoryInformationBuilders);
	}

	public Map<String, ResourceRegistryPart> getRegistryParts() {
		return Collections.unmodifiableMap(registryParts);
	}

	public void addFilter(DocumentFilter filter) {
		checkInitialized();
		filters.add(filter);
	}

	public void addRepositoryFilter(RepositoryFilter filter) {
		checkInitialized();
		repositoryFilters.add(filter);
	}

	public void addRepositoryDecoratorFactory(RepositoryDecoratorFactory decorator) {
		checkInitialized();
		repositoryDecoratorFactories.add(decorator);
	}

	protected List<DocumentFilter> getFilters() {
		checkInitialized();
		return Collections.unmodifiableList(filters);
	}

	protected List<RepositoryFilter> getRepositoryFilters() {
		checkInitialized();
		return Collections.unmodifiableList(repositoryFilters);
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

	@Deprecated
	public void addRepository(Class<?> resourceClass, Object repository) {
		checkInitialized();
		repositories.add(repository);
	}

	@Deprecated
	public void addRepository(Class<?> sourceType, Class<?> targetType, Object repository) {
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

	public void addHttpRequestProcessor(HttpRequestProcessor httpRequestProcessor) {
		httpRequestProcessors.add(httpRequestProcessor);
	}

	public List<HttpRequestProcessor> getHttpRequestProcessors() {
		return Collections.unmodifiableList(httpRequestProcessors);
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

		private Set<JsonApiExceptionMapper> set;

		private CollectionExceptionMapperLookup(Set<JsonApiExceptionMapper> set) {
			this.set = set;
		}

		public CollectionExceptionMapperLookup(JsonApiExceptionMapper exceptionMapper) {
			this(new HashSet<JsonApiExceptionMapper>(Arrays.asList(exceptionMapper)));
		}

		@Override
		public Set<JsonApiExceptionMapper> getExceptionMappers() {
			return set;
		}
	}
}
