package io.crnk.client;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
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
import io.crnk.client.legacy.RelationshipRepositoryStub;
import io.crnk.client.legacy.ResourceRepositoryStub;
import io.crnk.client.module.ClientModule;
import io.crnk.client.module.HttpAdapterAware;
import io.crnk.core.engine.information.repository.RepositoryInformationBuilder;
import io.crnk.core.engine.information.repository.ResourceRepositoryInformation;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.information.resource.ResourceInformationBuilder;
import io.crnk.core.engine.internal.exception.ExceptionMapperLookup;
import io.crnk.core.engine.internal.exception.ExceptionMapperRegistry;
import io.crnk.core.engine.internal.exception.ExceptionMapperRegistryBuilder;
import io.crnk.core.engine.internal.information.repository.ResourceRepositoryInformationImpl;
import io.crnk.core.engine.internal.jackson.JsonApiModuleBuilder;
import io.crnk.core.engine.internal.registry.ResourceRegistryImpl;
import io.crnk.core.engine.internal.repository.RelationshipRepositoryAdapter;
import io.crnk.core.engine.internal.repository.ResourceRepositoryAdapter;
import io.crnk.core.engine.internal.utils.JsonApiUrlBuilder;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.engine.internal.utils.UrlUtils;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.engine.registry.ResponseRelationshipEntry;
import io.crnk.core.engine.url.ConstantServiceUrlProvider;
import io.crnk.core.engine.url.ServiceUrlProvider;
import io.crnk.core.exception.InvalidResourceException;
import io.crnk.core.module.Module;
import io.crnk.core.module.ModuleRegistry;
import io.crnk.core.module.discovery.ResourceLookup;
import io.crnk.core.module.internal.DefaultRepositoryInformationBuilderContext;
import io.crnk.core.repository.RelationshipRepositoryV2;
import io.crnk.core.repository.ResourceRepositoryV2;
import io.crnk.core.resource.list.DefaultResourceList;
import io.crnk.legacy.internal.DirectResponseRelationshipEntry;
import io.crnk.legacy.internal.DirectResponseResourceEntry;
import io.crnk.legacy.registry.RepositoryInstanceBuilder;
import io.crnk.legacy.repository.RelationshipRepository;

/**
 * Client implementation giving access to JSON API repositories using stubs.
 */
public class CrnkClient {

	private HttpAdapter httpAdapter;

	private ObjectMapper objectMapper;

	private ResourceRegistry resourceRegistry;

	private ModuleRegistry moduleRegistry;

	private JsonApiUrlBuilder urlBuilder;

	private boolean initialized = false;

	private ExceptionMapperRegistry exceptionMapperRegistry;

	private boolean pushAlways = false;

	private ActionStubFactory actionStubFactory;

	private ClientDocumentMapper documentMapper;

	private List<HttpAdapterProvider> httpAdapterProviders = new ArrayList<>();

	public CrnkClient(String serviceUrl) {
		this(new ConstantServiceUrlProvider(UrlUtils.removeTrailingSlash(serviceUrl)));
	}

	public CrnkClient(ServiceUrlProvider serviceUrlProvider) {
		this.registerHttpAdapterProvider(new OkHttpAdapterProvider());
		this.registerHttpAdapterProvider(new HttpClientAdapterProvider());

		moduleRegistry = new ModuleRegistry(false);

		moduleRegistry.addModule(new ClientModule());

		resourceRegistry = new ClientResourceRegistry(moduleRegistry, serviceUrlProvider);
		urlBuilder = new JsonApiUrlBuilder(resourceRegistry);

		objectMapper = new ObjectMapper();
		objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

		// consider use of crnk module in the future
		JsonApiModuleBuilder moduleBuilder = new JsonApiModuleBuilder();
		SimpleModule jsonApiModule = moduleBuilder.build();
		objectMapper.registerModule(jsonApiModule);

		documentMapper = new ClientDocumentMapper(moduleRegistry, objectMapper, null);
		setProxyFactory(new BasicProxyFactory());
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
				final ResourceRepositoryStubImpl<T, ?> repositoryStub =
						new ResourceRepositoryStubImpl<>(CrnkClient.this, resourceClass, resourceInformation, urlBuilder);
				return repositoryStub.findAll(url);

			}
		});
		documentMapper.setProxyFactory(proxyFactory);
	}


	public void registerHttpAdapterProvider(HttpAdapterProvider httpAdapterProvider) {
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

	public boolean getPushAlways() {
		return pushAlways;
	}

	/**
	 * Older CrnkClient implementation only supported a save() operation
	 * that POSTs the resource to the server. No difference is made between
	 * insert and update. The server-implementation still does not make a
	 * difference.
	 * <p>
	 * By default the flag is enabled to maintain backward compatibility. But it
	 * is strongly adviced to turn id on. It will become the default in one of
	 * the subsequent releases.
	 */
	public void setPushAlways(boolean pushAlways) {
		this.pushAlways = pushAlways;
	}

	protected void init() {
		if (initialized) {
			return;
		}
		initialized = true;

		initHttpAdapter();

		initModuleRegistry();
		initExceptionMapperRegistry();
		initResources();
	}

	private void initHttpAdapter() {
		if (httpAdapter == null) {
			httpAdapter = detectHttpAdapter();
		}
	}

	private void initResources() {
		ResourceLookup resourceLookup = moduleRegistry.getResourceLookup();
		for (Class<?> resourceClass : resourceLookup.getResourceClasses()) {
			getQuerySpecRepository(resourceClass);
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
	private <T, I extends Serializable> RegistryEntry allocateRepository(Class<T> resourceClass, boolean allocateRelated) {
		ResourceInformationBuilder resourceInformationBuilder = moduleRegistry.getResourceInformationBuilder();

		ResourceInformation resourceInformation = resourceInformationBuilder.build(resourceClass);
		final ResourceRepositoryStub<T, I> repositoryStub =
				new ResourceRepositoryStubImpl<>(this, resourceClass, resourceInformation, urlBuilder);

		// create interface for it!
		RepositoryInstanceBuilder repositoryInstanceBuilder = new RepositoryInstanceBuilder(null, null) {

			@Override
			public Object buildRepository() {
				return repositoryStub;
			}
		};
		ResourceRepositoryInformation repositoryInformation =
				new ResourceRepositoryInformationImpl(repositoryStub.getClass(), resourceInformation.getResourceType(),
						resourceInformation);
		ResourceEntry resourceEntry = new DirectResponseResourceEntry(repositoryInstanceBuilder);
		List<ResponseRelationshipEntry> relationshipEntries = new ArrayList<>();
		RegistryEntry registryEntry = new RegistryEntry(repositoryInformation, resourceEntry, relationshipEntries);
		resourceRegistry.addEntry(resourceClass, registryEntry);

		allocateRepositoryRelations(registryEntry, allocateRelated, relationshipEntries);

		return registryEntry;
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private void allocateRepositoryRelations(RegistryEntry registryEntry, boolean allocateRelated,
			List<ResponseRelationshipEntry> relationshipEntries) {
		ResourceInformation resourceInformation = registryEntry.getResourceInformation();
		List<ResourceField> relationshipFields = resourceInformation.getRelationshipFields();
		for (ResourceField relationshipField : relationshipFields) {
			final Class<?> targetClass = relationshipField.getElementType();
			Class<?> resourceClass = resourceInformation.getResourceClass();

			final RelationshipRepositoryStubImpl relationshipRepositoryStub =
					new RelationshipRepositoryStubImpl(this, resourceClass, targetClass, resourceInformation, urlBuilder);
			RepositoryInstanceBuilder<RelationshipRepository> relationshipRepositoryInstanceBuilder =
					new RepositoryInstanceBuilder<RelationshipRepository>(null, null) {

						@Override
						public RelationshipRepository buildRepository() {
							return relationshipRepositoryStub;
						}
					};
			DirectResponseRelationshipEntry relationshipEntry =
					new DirectResponseRelationshipEntry(relationshipRepositoryInstanceBuilder) {

						@Override
						public Class<?> getTargetAffiliation() {
							return targetClass;
						}
					};
			relationshipEntries.add(relationshipEntry);

			// allocate relations as well
			if (allocateRelated) {
				ClientResourceRegistry clientResourceRegistry = (ClientResourceRegistry) resourceRegistry;
				if (!clientResourceRegistry.isInitialized(targetClass)) {
					allocateRepository(targetClass, true);
				}
			}
		}
	}

	/**
	 * @deprecated Make use of getRepositoryForInterface.
	 */
	@Deprecated
	public <R extends ResourceRepositoryV2<?, ?>> R getResourceRepository(Class<R> repositoryInterfaceClass) {
		return getRepositoryForInterface(repositoryInterfaceClass);
	}

	@SuppressWarnings("unchecked")
	public <R extends ResourceRepositoryV2<?, ?>> R getRepositoryForInterface(Class<R> repositoryInterfaceClass) {
		RepositoryInformationBuilder informationBuilder = moduleRegistry.getRepositoryInformationBuilder();
		PreconditionUtil.assertTrue("no a valid repository interface", informationBuilder.accept(repositoryInterfaceClass));
		ResourceRepositoryInformation repositoryInformation = (ResourceRepositoryInformation) informationBuilder
				.build(repositoryInterfaceClass, new DefaultRepositoryInformationBuilderContext(moduleRegistry));
		Class<?> resourceClass = repositoryInformation.getResourceInformation().getResourceClass();

		Object actionStub = actionStubFactory != null ? actionStubFactory.createStub(repositoryInterfaceClass) : null;
		ResourceRepositoryV2<?, Serializable> repositoryStub = getQuerySpecRepository(resourceClass);

		ClassLoader classLoader = repositoryInterfaceClass.getClassLoader();
		InvocationHandler invocationHandler =
				new ClientStubInvocationHandler(repositoryInterfaceClass, repositoryStub, actionStub);
		return (R) Proxy.newProxyInstance(classLoader, new Class[] {repositoryInterfaceClass, ResourceRepositoryV2.class},
				invocationHandler);
	}

	/**
	 * @param resourceClass resource class
	 * @return stub for the given resourceClass
	 * @deprecated make use of QuerySpec
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	@Deprecated
	public <T, I extends Serializable> ResourceRepositoryStub<T, I> getQueryParamsRepository(Class<T> resourceClass) {
		init();

		RegistryEntry entry = resourceRegistry.findEntry(resourceClass);

		// TODO fix this in crnk, should be able to get original document
		ResourceRepositoryAdapter repositoryAdapter = entry.getResourceRepository(null);
		return (ResourceRepositoryStub<T, I>) repositoryAdapter.getResourceRepository();
	}

	/**
	 * @param resourceClass repository class
	 * @return stub for the given resourceClass
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	public <T, I extends Serializable> ResourceRepositoryV2<T, I> getRepositoryForType(Class<T> resourceClass) {
		init();

		RegistryEntry entry = resourceRegistry.findEntry(resourceClass);
		ResourceRepositoryAdapter repositoryAdapter = entry.getResourceRepository(null);
		return (ResourceRepositoryV2<T, I>) repositoryAdapter.getResourceRepository();

	}

	/**
	 * @deprecated make use of getRepositoryForType()
	 */
	@Deprecated
	public <T, I extends Serializable> ResourceRepositoryV2<T, I> getQuerySpecRepository(Class<T> resourceClass) {
		return getRepositoryForType(resourceClass);
	}

	/**
	 * @param sourceClass source class
	 * @param targetClass target class
	 * @return stub for the relationship between the given source and target
	 * class
	 * @deprecated make use of QuerySpec
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	public <T, I extends Serializable, D, J extends Serializable> RelationshipRepositoryStub<T, I, D, J>
	getQueryParamsRepository(
			Class<T> sourceClass, Class<D> targetClass) {
		init();

		RegistryEntry entry = resourceRegistry.findEntry(sourceClass);

		RelationshipRepositoryAdapter repositoryAdapter = entry.getRelationshipRepositoryForClass(targetClass, null);
		return (RelationshipRepositoryStub<T, I, D, J>) repositoryAdapter.getRelationshipRepository();
	}

	/**
	 * @param sourceClass source class
	 * @param targetClass target class
	 * @return stub for the relationship between the given source and target
	 * class
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	public <T, I extends Serializable, D, J extends Serializable> RelationshipRepositoryV2<T, I, D, J> getRepositoryForType(
			Class<T> sourceClass, Class<D> targetClass) {
		init();

		RegistryEntry entry = resourceRegistry.findEntry(sourceClass);

		RelationshipRepositoryAdapter repositoryAdapter = entry.getRelationshipRepositoryForClass(targetClass, null);
		return (RelationshipRepositoryV2<T, I, D, J>) repositoryAdapter.getRelationshipRepository();
	}

	/**
	 * @deprecated make use of getRepositoryForType()
	 */
	@Deprecated
	public <T, I extends Serializable, D, J extends Serializable> RelationshipRepositoryV2<T, I, D, J> getQuerySpecRepository(
			Class<T> sourceClass, Class<D> targetClass) {
		return getRepositoryForType(sourceClass, targetClass);
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
		PreconditionUtil.assertFalse("already initialized, cannot add module", initialized);
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
					return moduleRegistry.getResourceRegistry().getServiceUrlProvider();
				}

				@Override
				public HttpAdapter getHttpAdapter() {
					return httpAdapter;
				}
			});
		}
	}

	public ModuleRegistry getModuleRegistry() {
		return moduleRegistry;
	}

	public ClientDocumentMapper getDocumentMapper() {
		return documentMapper;
	}

	class ClientResourceRegistry extends ResourceRegistryImpl {

		public ClientResourceRegistry(ModuleRegistry moduleRegistry, ServiceUrlProvider serviceUrlProvider) {
			super(moduleRegistry, serviceUrlProvider);
		}

		@Override
		protected synchronized RegistryEntry findEntry(Class<?> clazz, boolean allowNull) {
			RegistryEntry entry = getEntry(clazz);
			if (entry == null) {
				ResourceInformationBuilder informationBuilder = moduleRegistry.getResourceInformationBuilder();
				if (!informationBuilder.accept(clazz)) {
					throw new InvalidResourceException(clazz.getName() + " not recognized as resource class, consider adding "
							+ "@JsonApiResource annotation");
				}
				entry = allocateRepository(clazz, true);
			}
			return entry;
		}

		public boolean isInitialized(Class<?> clazz) {
			return super.findEntry(clazz, true) != null;
		}
	}
}
