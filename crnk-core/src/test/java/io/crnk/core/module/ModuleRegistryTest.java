package io.crnk.core.module;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.crnk.core.repository.InMemoryResourceRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.crnk.core.engine.error.JsonApiExceptionMapper;
import io.crnk.core.engine.filter.DocumentFilter;
import io.crnk.core.engine.filter.RepositoryFilter;
import io.crnk.core.engine.filter.ResourceModificationFilter;
import io.crnk.core.engine.information.InformationBuilder;
import io.crnk.core.engine.information.repository.RepositoryInformationProvider;
import io.crnk.core.engine.information.repository.RepositoryInformationProviderContext;
import io.crnk.core.engine.information.repository.ResourceRepositoryInformation;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.information.resource.ResourceInformationProvider;
import io.crnk.core.engine.information.resource.ResourceInformationProviderModule;
import io.crnk.core.engine.internal.CoreModule;
import io.crnk.core.engine.internal.dispatcher.filter.TestFilter;
import io.crnk.core.engine.internal.dispatcher.filter.TestRepositoryDecorator;
import io.crnk.core.engine.internal.dispatcher.filter.TestRepositoryDecorator.DecoratedScheduleRepository;
import io.crnk.core.engine.internal.exception.ExceptionMapperLookup;
import io.crnk.core.engine.internal.exception.ExceptionMapperRegistryTest.IllegalStateExceptionMapper;
import io.crnk.core.engine.internal.exception.ExceptionMapperRegistryTest.SomeIllegalStateExceptionMapper;
import io.crnk.core.engine.internal.information.DefaultInformationBuilder;
import io.crnk.core.engine.internal.jackson.JacksonModule;
import io.crnk.core.engine.internal.registry.ResourceRegistryImpl;
import io.crnk.core.engine.internal.repository.RelationshipRepositoryAdapter;
import io.crnk.core.engine.parser.TypeParser;
import io.crnk.core.engine.registry.DefaultResourceRegistryPart;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.engine.security.SecurityProvider;
import io.crnk.core.engine.url.ConstantServiceUrlProvider;
import io.crnk.core.mock.models.ComplexPojo;
import io.crnk.core.mock.models.Document;
import io.crnk.core.mock.models.FancyProject;
import io.crnk.core.mock.models.Project;
import io.crnk.core.mock.models.Schedule;
import io.crnk.core.mock.models.Task;
import io.crnk.core.mock.models.Thing;
import io.crnk.core.mock.models.User;
import io.crnk.core.mock.repository.DocumentRepository;
import io.crnk.core.mock.repository.PojoRepository;
import io.crnk.core.mock.repository.ProjectRepository;
import io.crnk.core.mock.repository.RelationIdTestRepository;
import io.crnk.core.mock.repository.ResourceWithoutRepositoryToProjectRepository;
import io.crnk.core.mock.repository.ScheduleRepository;
import io.crnk.core.mock.repository.ScheduleRepositoryImpl;
import io.crnk.core.mock.repository.TaskRepository;
import io.crnk.core.mock.repository.TaskToProjectRepository;
import io.crnk.core.mock.repository.TaskWithLookupRepository;
import io.crnk.core.mock.repository.UserRepository;
import io.crnk.core.mock.repository.UserToProjectRepository;
import io.crnk.core.module.discovery.ResourceLookup;
import io.crnk.core.module.discovery.ServiceDiscovery;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.queryspec.pagingspec.OffsetLimitPagingBehavior;
import io.crnk.core.repository.RelationshipRepositoryV2;
import io.crnk.core.repository.ResourceRepositoryV2;
import io.crnk.core.repository.decorate.RepositoryDecoratorFactory;
import io.crnk.core.repository.decorate.RepositoryDecoratorFactoryBase;
import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiRelation;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.core.utils.Prioritizable;

public class ModuleRegistryTest {

	private ResourceRegistry resourceRegistry;

	private ModuleRegistry moduleRegistry;

	private TestModule testModule;

	private ServiceDiscovery serviceDiscovery = Mockito.mock(ServiceDiscovery.class);

	@Before
	public void setup() {
		moduleRegistry = new ModuleRegistry();
		moduleRegistry.getHttpRequestContextProvider().setServiceUrlProvider(new ConstantServiceUrlProvider("http://localhost"));
		resourceRegistry = new ResourceRegistryImpl(new DefaultResourceRegistryPart(), moduleRegistry);

		moduleRegistry.setServiceDiscovery(serviceDiscovery);

		testModule = new TestModule();
		moduleRegistry.addModule(testModule);
		moduleRegistry.addModule(new CoreModule());
		moduleRegistry.addModule(new JacksonModule(new ObjectMapper(), false));

		moduleRegistry.addPagingBehavior(new OffsetLimitPagingBehavior());
		moduleRegistry.addModule(new ResourceInformationProviderModule());
		moduleRegistry.init(new ObjectMapper());

		Assert.assertEquals(resourceRegistry, moduleRegistry.getResourceRegistry());
	}

	interface PrioDocumentFilter extends DocumentFilter, Prioritizable {

	}

	@Test
	public void checkAddingPagingBehavior(){
		Assert.assertEquals(1, moduleRegistry.getPagingBehaviors().size());
	}

	@Test
	public void checkDocumentFilterPriority() {
		PrioDocumentFilter filter1 = Mockito.mock(PrioDocumentFilter.class);
		PrioDocumentFilter filter2 = Mockito.mock(PrioDocumentFilter.class);
		Mockito.when(filter1.getPriority()).thenReturn(2);
		Mockito.when(filter2.getPriority()).thenReturn(1);

		ModuleRegistry moduleRegistry = new ModuleRegistry();
		SimpleModule module = new SimpleModule("test");
		module.addFilter(filter1);
		module.addFilter(filter2);
		moduleRegistry.addModule(module);
		moduleRegistry.init(new ObjectMapper());

		List<DocumentFilter> filters = moduleRegistry.getFilters();
		Assert.assertSame(filter2, filters.get(0));
		Assert.assertSame(filter1, filters.get(1));
	}


	interface PrioResourceModificationFilter extends ResourceModificationFilter, Prioritizable {

	}


	@Test
	public void checkResourceModificationFilterPriority() {
		PrioResourceModificationFilter filter1 = Mockito.mock(PrioResourceModificationFilter.class);
		PrioResourceModificationFilter filter2 = Mockito.mock(PrioResourceModificationFilter.class);
		Mockito.when(filter1.getPriority()).thenReturn(2);
		Mockito.when(filter2.getPriority()).thenReturn(1);

		ModuleRegistry moduleRegistry = new ModuleRegistry();
		SimpleModule module = new SimpleModule("test");
		module.addResourceModificationFilter(filter1);
		module.addResourceModificationFilter(filter2);
		moduleRegistry.addModule(module);
		moduleRegistry.init(new ObjectMapper());

		List<ResourceModificationFilter> filters = moduleRegistry.getResourceModificationFilters();
		Assert.assertSame(filter2, filters.get(0));
		Assert.assertSame(filter1, filters.get(1));
	}

	@Test
	public void checkNullResourcePath(){
		ModuleRegistry moduleRegistry = new ModuleRegistry();
		SimpleModule module = new SimpleModule("test");
		moduleRegistry.addModule(module);
		moduleRegistry.init(new ObjectMapper());
		Assert.assertEquals(moduleRegistry.getResourceInformationBuilder().getResourcePath(TestResource2.class), null);
	}

	interface PrioRepositoryFilter extends RepositoryFilter, Prioritizable {

	}


	@Test
	public void checkRepositoryFilterPriority() {
		PrioRepositoryFilter filter1 = Mockito.mock(PrioRepositoryFilter.class);
		PrioRepositoryFilter filter2 = Mockito.mock(PrioRepositoryFilter.class);
		Mockito.when(filter1.getPriority()).thenReturn(2);
		Mockito.when(filter2.getPriority()).thenReturn(1);

		ModuleRegistry moduleRegistry = new ModuleRegistry();
		SimpleModule module = new SimpleModule("test");
		module.addRepositoryFilter(filter1);
		module.addRepositoryFilter(filter2);
		moduleRegistry.addModule(module);
		moduleRegistry.init(new ObjectMapper());

		List<RepositoryFilter> filters = moduleRegistry.getRepositoryFilters();
		Assert.assertSame(filter2, filters.get(0));
		Assert.assertSame(filter1, filters.get(1));
	}


	@Test
	public void getModules() {
		Assert.assertEquals(4, moduleRegistry.getModules().size());
	}

	@Test
	public void testGetServiceDiscovery() {
		Assert.assertEquals(serviceDiscovery, moduleRegistry.getServiceDiscovery());
		Assert.assertEquals(serviceDiscovery, testModule.context.getServiceDiscovery());
	}

	@Test(expected = UnsupportedOperationException.class)
	public void invalidRepository() {
		moduleRegistry.getRepositoryInformationBuilder().build("no resource", null);
	}

	@Test
	public void getModuleContext() {
		Assert.assertNotNull(moduleRegistry.getContext());
		Assert.assertNotNull(moduleRegistry.getContext().getObjectMapper());
	}

	@Test
	public void repositoryInformationBuilderAccept() {
		RepositoryInformationProvider builder = moduleRegistry.getRepositoryInformationBuilder();
		Assert.assertFalse(builder.accept("no resource"));
		Assert.assertFalse(builder.accept(String.class));
		Assert.assertTrue(builder.accept(TaskRepository.class));
		Assert.assertTrue(builder.accept(ProjectRepository.class));
		Assert.assertTrue(builder.accept(TaskToProjectRepository.class));
		Assert.assertTrue(builder.accept(new TaskRepository()));
		Assert.assertTrue(builder.accept(new TaskToProjectRepository()));
	}

	@Test(expected = UnsupportedOperationException.class)
	public void buildWithInvalidRepositoryClass() {
		RepositoryInformationProviderContext context = Mockito.mock(RepositoryInformationProviderContext.class);
		moduleRegistry.getRepositoryInformationBuilder().build(String.class, context);
	}

	@Test
	public void buildResourceRepositoryInformationFromClass() {
		RepositoryInformationProvider builder = moduleRegistry.getRepositoryInformationBuilder();

		ResourceRepositoryInformation info =
				(ResourceRepositoryInformation) builder.build(TaskRepository.class, newRepositoryInformationBuilderContext());
		Assert.assertEquals(Task.class, info.getResourceInformation().get().getResourceClass());
		Assert.assertEquals("tasks", info.getPath());
	}

	@Test
	public void buildResourceRepositoryInformationFromInstance() {
		RepositoryInformationProvider builder = moduleRegistry.getRepositoryInformationBuilder();

		ResourceRepositoryInformation info =
				(ResourceRepositoryInformation) builder.build(new TaskRepository(), newRepositoryInformationBuilderContext());
		Assert.assertEquals(Task.class, info.getResourceInformation().get().getResourceClass());
		Assert.assertEquals("tasks", info.getPath());
	}

	private RepositoryInformationProviderContext newRepositoryInformationBuilderContext() {
		return new RepositoryInformationProviderContext() {

			@Override
			public ResourceInformationProvider getResourceInformationBuilder() {
				return moduleRegistry.getResourceInformationBuilder();
			}

			@Override
			public TypeParser getTypeParser() {
				return moduleRegistry.getTypeParser();
			}

			@Override
			public InformationBuilder builder() {
				return new DefaultInformationBuilder(getTypeParser());
			}
		};
	}

	@Test(expected = IllegalStateException.class)
	public void testNotInitialized() {
		moduleRegistry = new ModuleRegistry();
		moduleRegistry.getResourceRegistry();
	}

	@Test(expected = IllegalStateException.class)
	public void testDuplicateInitialization() {
		ObjectMapper objectMapper = new ObjectMapper();
		moduleRegistry.init(objectMapper);
	}

	@Test
	public void checkGetModule() {
		Module notRegisteredModule = Mockito.mock(Module.class);
		Assert.assertNotNull(moduleRegistry.getModule(TestModule.class).get());
		Assert.assertFalse(moduleRegistry.getModule(notRegisteredModule.getClass()).isPresent());
	}


	@Test
	public void testExceptionMappers() {
		ExceptionMapperLookup exceptionMapperLookup = moduleRegistry.getExceptionMapperLookup();
		Set<JsonApiExceptionMapper> exceptionMappers = exceptionMapperLookup.getExceptionMappers();
		Set<Class<?>> classes = new HashSet<>();
		for (JsonApiExceptionMapper exceptionMapper : exceptionMappers) {
			classes.add(exceptionMapper.getClass());
		}
		Assert.assertTrue(classes.contains(IllegalStateExceptionMapper.class));
		Assert.assertTrue(classes.contains(SomeIllegalStateExceptionMapper.class));
	}

	@Test
	public void testInitCalled() {
		Assert.assertTrue(testModule.initialized);
	}

	@Test(expected = IllegalStateException.class)
	public void testModuleChangeAfterAddModule() {
		SimpleModule module = new SimpleModule("test2");
		moduleRegistry.addModule(module);
		module.addFilter(new TestFilter());
	}

	@Test
	public void testGetResourceRegistry() {
		Assert.assertSame(resourceRegistry, testModule.getContext().getResourceRegistry());
	}

	@Test(expected = IllegalStateException.class)
	public void testNoResourceRegistryBeforeInitialization() {
		ModuleRegistry registry = new ModuleRegistry();
		registry.addModule(new SimpleModule("test") {

			@Override
			public void setupModule(ModuleContext context) {
				context.getResourceRegistry(); // fail
			}
		});
	}

	@Test
	public void testInformationBuilder() throws Exception {
		ResourceInformationProvider informationProvider = moduleRegistry.getResourceInformationBuilder();

		Assert.assertTrue(informationProvider.accept(ComplexPojo.class));
		Assert.assertTrue(informationProvider.accept(Document.class));
		Assert.assertTrue(informationProvider.accept(FancyProject.class));
		Assert.assertTrue(informationProvider.accept(Project.class));
		Assert.assertTrue(informationProvider.accept(Task.class));
		Assert.assertTrue(informationProvider.accept(Thing.class));
		Assert.assertTrue(informationProvider.accept(User.class));
		Assert.assertTrue(informationProvider.accept(TestResource.class));

		Assert.assertFalse(informationProvider.accept(TestRepository.class));
		Assert.assertFalse(informationProvider.accept(DocumentRepository.class));
		Assert.assertFalse(informationProvider.accept(PojoRepository.class));
		Assert.assertFalse(informationProvider.accept(ProjectRepository.class));
		Assert.assertFalse(informationProvider.accept(ResourceWithoutRepositoryToProjectRepository.class));
		Assert.assertFalse(informationProvider.accept(TaskToProjectRepository.class));
		Assert.assertFalse(informationProvider.accept(TaskWithLookupRepository.class));
		Assert.assertFalse(informationProvider.accept(UserRepository.class));
		Assert.assertFalse(informationProvider.accept(UserToProjectRepository.class));

		Assert.assertFalse(informationProvider.accept(Object.class));
		Assert.assertFalse(informationProvider.accept(String.class));

		try {
			informationProvider.build(Object.class);
			Assert.fail();
		}
		catch (UnsupportedOperationException e) {
			// ok
		}

		ResourceInformation userInfo = informationProvider.build(User.class);
		Assert.assertEquals("id", userInfo.getIdField().getUnderlyingName());

		ResourceInformation testInfo = informationProvider.build(TestResource.class);
		Assert.assertEquals("id", testInfo.getIdField().getUnderlyingName());

		// setup by TestResourceInformationProvider
		Assert.assertEquals("testId", testInfo.getIdField().getJsonName());
	}

	@Test
	public void testResourceLookup() throws Exception {
		ResourceLookup resourceLookup = moduleRegistry.getResourceLookup();

		Assert.assertFalse(resourceLookup.getResourceClasses().contains(Object.class));
		Assert.assertFalse(resourceLookup.getResourceClasses().contains(String.class));
		Assert.assertTrue(resourceLookup.getResourceClasses().contains(TestResource.class));
	}

	@Test
	public void testJacksonModule() throws Exception {
		List<com.fasterxml.jackson.databind.Module> jacksonModules = moduleRegistry.getJacksonModules();
		Assert.assertEquals(1, jacksonModules.size());
		com.fasterxml.jackson.databind.Module jacksonModule = jacksonModules.get(0);
		Assert.assertEquals("test", jacksonModule.getModuleName());
	}

	@Test
	public void testFilter() throws Exception {
		List<DocumentFilter> filters = moduleRegistry.getFilters();
		Assert.assertEquals(1, filters.size());
	}

	@Test
	public void checkCombinedResourceInformationBuilderGetResurceType() {
		Class<?> noResourceClass = String.class;
		Assert.assertNull(moduleRegistry.getResourceInformationBuilder().getResourceType(noResourceClass));
		Assert.assertNotNull(moduleRegistry.getResourceInformationBuilder().getResourceType(Task.class));
	}

	@Test
	public void testDecorators() throws Exception {
		List<RepositoryDecoratorFactory> decorators = moduleRegistry.getRepositoryDecoratorFactories();
		Assert.assertEquals(2, decorators.size());

		RegistryEntry entry = this.resourceRegistry.getEntry(Schedule.class);
		Object resourceRepository = entry.getResourceRepository(null).getResourceRepository();
		Assert.assertNotNull(resourceRepository);
		Assert.assertTrue(resourceRepository instanceof ScheduleRepository);
		Assert.assertTrue(resourceRepository instanceof DecoratedScheduleRepository);
	}

	@Test
	public void testSecurityProvider() throws Exception {
		Assert.assertTrue(moduleRegistry.getSecurityProvider().isUserInRole("testRole"));
		Assert.assertFalse(moduleRegistry.getSecurityProvider().isUserInRole("nonExistingRole"));
		Assert.assertTrue(testModule.getContext().getSecurityProvider().isUserInRole("testRole"));
	}

	@Test
	public void testRepositoryRegistration() {
		RegistryEntry entry = resourceRegistry.getEntry(TestResource2.class);
		ResourceInformation info = entry.getResourceInformation();
		Assert.assertEquals(TestResource2.class, info.getResourceClass());

		Assert.assertNotNull(entry.getResourceRepository(null));
		RelationshipRepositoryAdapter relationshipRepositoryAdapter = entry.getRelationshipRepository("parent", null);
		Assert.assertNotNull(relationshipRepositoryAdapter);
	}

	@JsonApiResource(type = "test2")
	static class TestResource2 {

		@JsonApiId
		private int id;

		@JsonApiRelation
		private TestResource2 parent;

		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}

		public TestResource2 getParent() {
			return parent;
		}

		public void setParent(TestResource2 parent) {
			this.parent = parent;
		}
	}

	@JsonApiResource(type = "test3", resourcePath = "/testResource3s")
	static class TestResource3 {

		@JsonApiId
		private int id;
	}

	class TestModule implements InitializingModule {

		private ModuleContext context;

		private boolean initialized;

		@Override
		public String getModuleName() {
			return "test";
		}

		public ModuleContext getContext() {
			return context;
		}

		@Override
		public void setupModule(ModuleContext context) {
			this.context = context;
			context.addResourceLookup(new TestResourceLookup());
			context.addResourceInformationBuilder(new TestResourceInformationProvider());

			context.addJacksonModule(new com.fasterxml.jackson.databind.module.SimpleModule() {

				private static final long serialVersionUID = 7829254359521781942L;

				@Override
				public String getModuleName() {
					return "test";
				}
			});

			context.addSecurityProvider(new SecurityProvider() {

				@Override
				public boolean isUserInRole(String role) {
					return "testRole".equals(role);
				}
			});

			context.addRepositoryDecoratorFactory(new TestRepositoryDecorator());
			context.addRepositoryDecoratorFactory(new RepositoryDecoratorFactoryBase());
			context.addFilter(new TestFilter());
			context.addRepository(new ScheduleRepositoryImpl());
			context.addRepository(new RelationIdTestRepository());
			context.addRepository(TestResource2.class, new TestRepository2());
			context.addRepository(TestResource2.class, TestResource2.class, new TestRelationshipRepository2());

			context.addRepository(new InMemoryResourceRepository<>(TestResource.class));

			context.addExceptionMapper(new IllegalStateExceptionMapper());
			context.addExceptionMapperLookup(new ExceptionMapperLookup() {

				@Override
				public Set<JsonApiExceptionMapper> getExceptionMappers() {
					Set<JsonApiExceptionMapper> set = new HashSet<>();
					set.add(new SomeIllegalStateExceptionMapper());
					return set;
				}
			});
		}

		@Override
		public void init() {
			initialized = true;
		}
	}

	class TestRelationshipRepository2 implements RelationshipRepositoryV2<TestResource2, Integer, TestResource2, Integer> {

		@Override
		public void setRelation(TestResource2 source, Integer targetId, String fieldName) {
		}

		@Override
		public void setRelations(TestResource2 source, Iterable<Integer> targetIds, String fieldName) {
		}

		@Override
		public void addRelations(TestResource2 source, Iterable<Integer> targetIds, String fieldName) {
		}

		@Override
		public void removeRelations(TestResource2 source, Iterable<Integer> targetIds, String fieldName) {
		}

		@Override
		public TestResource2 findOneTarget(Integer sourceId, String fieldName, QuerySpec queryParams) {
			return null;
		}

		@Override
		public ResourceList<TestResource2> findManyTargets(Integer sourceId, String fieldName, QuerySpec queryParams) {
			return null;
		}

		@Override
		public Class<TestResource2> getSourceResourceClass() {
			return TestResource2.class;
		}

		@Override
		public Class<TestResource2> getTargetResourceClass() {
			return TestResource2.class;
		}
	}

	class TestRepository2 implements ResourceRepositoryV2<TestResource2, Integer> {

		@Override
		public <S extends TestResource2> S save(S entity) {
			return null;
		}

		@Override
		public void delete(Integer id) {
		}

		@Override
		public Class<TestResource2> getResourceClass() {
			return TestResource2.class;
		}

		@Override
		public TestResource2 findOne(Integer id, QuerySpec querySpec) {
			return null;
		}

		@Override
		public ResourceList<TestResource2> findAll(QuerySpec querySpec) {
			return null;
		}

		@Override
		public ResourceList<TestResource2> findAll(Iterable<Integer> ids, QuerySpec querySpec) {
			return null;
		}

		@Override
		public <S extends TestResource2> S create(S entity) {
			return null;
		}
	}
}
