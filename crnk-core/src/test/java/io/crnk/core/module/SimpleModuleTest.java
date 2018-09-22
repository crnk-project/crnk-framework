package io.crnk.core.module;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.core.engine.dispatcher.RequestDispatcher;
import io.crnk.core.engine.error.ExceptionMapper;
import io.crnk.core.engine.error.JsonApiExceptionMapper;
import io.crnk.core.engine.filter.DocumentFilter;
import io.crnk.core.engine.filter.RepositoryFilter;
import io.crnk.core.engine.filter.ResourceFilter;
import io.crnk.core.engine.filter.ResourceFilterDirectory;
import io.crnk.core.engine.filter.ResourceModificationFilter;
import io.crnk.core.engine.http.HttpRequestProcessor;
import io.crnk.core.engine.information.contributor.ResourceFieldContributor;
import io.crnk.core.engine.information.repository.RepositoryInformationProvider;
import io.crnk.core.engine.information.resource.ResourceInformationProvider;
import io.crnk.core.engine.internal.dispatcher.filter.TestFilter;
import io.crnk.core.engine.internal.dispatcher.filter.TestRepositoryDecorator;
import io.crnk.core.engine.internal.exception.CrnkExceptionMapper;
import io.crnk.core.engine.internal.exception.ExceptionMapperLookup;
import io.crnk.core.engine.internal.exception.ExceptionMapperRegistry;
import io.crnk.core.engine.internal.exception.ExceptionMapperRegistryTest.IllegalStateExceptionMapper;
import io.crnk.core.engine.internal.registry.ResourceRegistryImpl;
import io.crnk.core.engine.internal.repository.RepositoryAdapterFactory;
import io.crnk.core.engine.parser.TypeParser;
import io.crnk.core.engine.properties.NullPropertiesProvider;
import io.crnk.core.engine.properties.PropertiesProvider;
import io.crnk.core.engine.registry.DefaultResourceRegistryPart;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.RegistryEntryBuilder;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.engine.registry.ResourceRegistryPart;
import io.crnk.core.engine.result.ResultFactory;
import io.crnk.core.engine.security.SecurityProvider;
import io.crnk.core.module.Module.ModuleContext;
import io.crnk.core.module.discovery.ResourceLookup;
import io.crnk.core.module.discovery.ServiceDiscovery;
import io.crnk.core.queryspec.pagingspec.OffsetLimitPagingBehavior;
import io.crnk.core.queryspec.pagingspec.PagingBehavior;
import io.crnk.core.repository.decorate.RepositoryDecoratorFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SimpleModuleTest {

	private TestModuleContext context;

	private SimpleModule module;

	@Before
	public void setup() {
		context = new TestModuleContext();
		module = new SimpleModule("simple");
	}

	@Test
	public void testGetModuleName() {
		Assert.assertEquals("simple", module.getModuleName());
	}

	@Test
	public void testResourceInformationBuilder() {
		module.addResourceInformationProvider(new TestResourceInformationProvider());
		Assert.assertEquals(1, module.getResourceInformationProviders().size());
		module.setupModule(context);

		Assert.assertEquals(1, context.numResourceInformationBuilds);
		Assert.assertEquals(0, context.numResourceLookups);
		Assert.assertEquals(0, context.numFilters);
		Assert.assertEquals(0, context.numJacksonModules);
		Assert.assertEquals(0, context.numRepositories);
	}

	@Test
	public void testRepositoryInformationBuilder() {
		module.addRepositoryInformationBuilder(Mockito.mock(RepositoryInformationProvider.class));
		Assert.assertEquals(1, module.getRepositoryInformationProviders().size());
		module.setupModule(context);

		Assert.assertEquals(1, context.numRepositoryInformationBuilds);
		Assert.assertEquals(0, context.numResourceInformationBuilds);
		Assert.assertEquals(0, context.numResourceLookups);
		Assert.assertEquals(0, context.numFilters);
		Assert.assertEquals(0, context.numJacksonModules);
		Assert.assertEquals(0, context.numRepositories);
	}

	@Test
	public void testResourceLookup() {
		module.addResourceLookup(new TestResourceLookup());
		Assert.assertEquals(1, module.getResourceLookups().size());
		module.setupModule(context);

		Assert.assertEquals(0, context.numResourceInformationBuilds);
		Assert.assertEquals(1, context.numResourceLookups);
		Assert.assertEquals(0, context.numFilters);
		Assert.assertEquals(0, context.numJacksonModules);
		Assert.assertEquals(0, context.numRepositories);
	}

	@Test
	public void testPagingBehaviorsBuilder() {
		module.addPagingBehavior(Mockito.mock(OffsetLimitPagingBehavior.class));
		Assert.assertEquals(1, module.getPagingBehaviors().size());
		module.setupModule(context);
		Assert.assertEquals(1, context.numPagingBehaviors);
		Assert.assertEquals(0, context.numResourceInformationBuilds);
		Assert.assertEquals(0, context.numResourceLookups);
		Assert.assertEquals(0, context.numFilters);
		Assert.assertEquals(0, context.numJacksonModules);
		Assert.assertEquals(0, context.numRepositories);
	}

	@Test
	public void testDuplicatePagingBehaviorRegistration() {
		module.addPagingBehavior(Mockito.mock(OffsetLimitPagingBehavior.class));

		// adding the same paging behavior a second time should cause an exception
		try {
			module.addPagingBehavior(Mockito.mock(OffsetLimitPagingBehavior.class));
			Assert.fail("IllegalArgumentException expected, paging was added already");
		} catch (IllegalArgumentException e) {
		}
	}

	@Test
	public void testFilter() {
		module.addFilter(new TestFilter());
		Assert.assertEquals(1, module.getFilters().size());
		module.setupModule(context);

		Assert.assertEquals(0, context.numResourceInformationBuilds);
		Assert.assertEquals(0, context.numResourceLookups);
		Assert.assertEquals(1, context.numFilters);
		Assert.assertEquals(0, context.numJacksonModules);
		Assert.assertEquals(0, context.numRepositories);
	}

	@Test
	public void testRepositoryDecorator() {
		module.addRepositoryDecoratorFactory(new TestRepositoryDecorator());
		Assert.assertEquals(1, module.getRepositoryDecoratorFactories().size());
		module.setupModule(context);

		Assert.assertEquals(0, context.numResourceInformationBuilds);
		Assert.assertEquals(0, context.numResourceLookups);
		Assert.assertEquals(1, context.numDecorators);
		Assert.assertEquals(0, context.numJacksonModules);
		Assert.assertEquals(0, context.numRepositories);
	}

	@Test
	public void testJacksonModule() {
		module.addJacksonModule(new com.fasterxml.jackson.databind.module.SimpleModule() {

			private static final long serialVersionUID = 7829254359521781942L;

			@Override
			public String getModuleName() {
				return "test";
			}
		});
		Assert.assertEquals(1, module.getJacksonModules().size());
		module.setupModule(context);

		Assert.assertEquals(0, context.numResourceInformationBuilds);
		Assert.assertEquals(0, context.numResourceLookups);
		Assert.assertEquals(0, context.numFilters);
		Assert.assertEquals(1, context.numJacksonModules);
		Assert.assertEquals(0, context.numRepositories);
	}

	@Test
	public void testAddRepository() {
		TestRelationshipRepository repository = new TestRelationshipRepository();
		module.addRepository(repository);
		Assert.assertEquals(1, module.getRepositories().size());

		module.setupModule(context);

		Assert.assertEquals(0, context.numResourceInformationBuilds);
		Assert.assertEquals(0, context.numResourceLookups);
		Assert.assertEquals(0, context.numFilters);
		Assert.assertEquals(0, context.numJacksonModules);
		Assert.assertEquals(1, context.numRepositories);
		Assert.assertEquals(0, context.numExceptionMapperLookup);
	}

	@Test
	public void testExceptionMapperLookup() {
		module.addExceptionMapperLookup(new TestExceptionMapperLookup());
		Assert.assertEquals(1, module.getExceptionMapperLookups().size());
		module.setupModule(context);

		Assert.assertEquals(0, context.numResourceInformationBuilds);
		Assert.assertEquals(0, context.numResourceLookups);
		Assert.assertEquals(0, context.numFilters);
		Assert.assertEquals(0, context.numJacksonModules);
		Assert.assertEquals(0, context.numRepositories);
		Assert.assertEquals(1, context.numExceptionMapperLookup);
	}

	@Test
	public void testAddExceptionMapper() {
		module.addExceptionMapper(new IllegalStateExceptionMapper());

		Assert.assertEquals(1, module.getExceptionMapperLookups().size());
		module.setupModule(context);

		Assert.assertEquals(0, context.numResourceInformationBuilds);
		Assert.assertEquals(0, context.numResourceLookups);
		Assert.assertEquals(0, context.numFilters);
		Assert.assertEquals(0, context.numJacksonModules);
		Assert.assertEquals(0, context.numRepositories);
		Assert.assertEquals(1, context.numExceptionMapperLookup);
	}

	class TestExceptionMapperLookup implements ExceptionMapperLookup {

		@SuppressWarnings("rawtypes")
		@Override
		public Set<JsonApiExceptionMapper> getExceptionMappers() {
			return new HashSet<JsonApiExceptionMapper>(Arrays.asList(new CrnkExceptionMapper()));
		}
	}

	class TestModuleContext implements ModuleContext {

		private int numResourceInformationBuilds = 0;

		private int numPagingBehaviors = 0;

		private int numRepositoryInformationBuilds = 0;

		private int numResourceLookups = 0;

		private int numJacksonModules = 0;

		private int numRepositories = 0;

		private int numFilters = 0;

		private int numExceptionMapperLookup = 0;

		private int numSecurityProviders = 0;

		private int numDecorators = 0;

		@Override
		public void addResourceInformationBuilder(ResourceInformationProvider resourceInformationProvider) {
			numResourceInformationBuilds++;
		}

		@Override
		public void addResourceLookup(ResourceLookup resourceLookup) {
			numResourceLookups++;
		}

		@Override
		public void addJacksonModule(Module module) {
			numJacksonModules++;
		}

		@Override
		public void addRepository(Class<?> resourceClass, Object repository) {
			numRepositories++;
		}

		@Override
		public void addRepository(Class<?> sourceResourceClass, Class<?> targetResourceClass, Object repository) {
			numRepositories++;
		}

		@Override
		public void addFilter(DocumentFilter filter) {
			numFilters++;
		}

		@Override
		public ResourceRegistry getResourceRegistry() {
			return new ResourceRegistryImpl(new DefaultResourceRegistryPart(), null);
		}

		@Override
		public void addExceptionMapperLookup(ExceptionMapperLookup exceptionMapperLookup) {
			numExceptionMapperLookup++;
		}

		@Override
		public void addExceptionMapper(ExceptionMapper<?> exceptionMapper) {
			numExceptionMapperLookup++;
		}

		@Override
		public void addSecurityProvider(SecurityProvider securityProvider) {
			numSecurityProviders++;
		}

		@Override
		public SecurityProvider getSecurityProvider() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setResultFactory(ResultFactory resultFactory) {
		}

		@Override
		public void addExtension(ModuleExtension extension) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void addHttpRequestProcessor(HttpRequestProcessor processor) {
			throw new UnsupportedOperationException();
		}

		@Override
		public ObjectMapper getObjectMapper() {
			return null;
		}

		@Override
		public void addRegistryPart(String prefix, ResourceRegistryPart part) {
			throw new UnsupportedOperationException();
		}

		@Override
		public ServiceDiscovery getServiceDiscovery() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void addRepositoryFilter(RepositoryFilter filter) {
			numFilters++;
		}

		@Override
		public void addResourceFilter(ResourceFilter filter) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void addResourceFieldContributor(ResourceFieldContributor contributor) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void addRepositoryInformationBuilder(RepositoryInformationProvider repositoryInformationProvider) {
			numRepositoryInformationBuilds++;
		}

		@Override
		public void addPagingBehavior(PagingBehavior pagingBehavior) {
			numPagingBehaviors++;
		}

		@Override
		public void addRepository(Object repository) {
			numRepositories++;
		}

		@Override
		public void addRepositoryDecoratorFactory(RepositoryDecoratorFactory decorator) {
			numDecorators++;
		}

		@Override
		public boolean isServer() {
			return true;
		}

		@Override
		public TypeParser getTypeParser() {
			return null;
		}

		@Override
		public ResourceInformationProvider getResourceInformationBuilder() {
			throw new UnsupportedOperationException();
		}

		@Override
		public ExceptionMapperRegistry getExceptionMapperRegistry() {
			throw new UnsupportedOperationException();
		}

		@Override
		public RequestDispatcher getRequestDispatcher() {
			throw new UnsupportedOperationException();
		}

		@Override
		public RegistryEntryBuilder newRegistryEntryBuilder() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void addRegistryEntry(RegistryEntry entry) {
			throw new UnsupportedOperationException();
		}

		@Override
		public ResourceFilterDirectory getResourceFilterDirectory() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void addResourceModificationFilter(ResourceModificationFilter filter) {
			throw new UnsupportedOperationException();
		}

		@Override
		public ResultFactory getResultFactory() {
			throw new UnsupportedOperationException();
		}

		@Override
		public List<DocumentFilter> getDocumentFilters() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void addRepositoryAdapterFactory(RepositoryAdapterFactory repositoryAdapterFactory) {
		}

		@Override
		public ModuleRegistry getModuleRegistry() {
			throw new UnsupportedOperationException();
		}

		@Override
		public PropertiesProvider getPropertiesProvider() {
			return new NullPropertiesProvider();
		}
	}
}
