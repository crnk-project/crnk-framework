package io.crnk.core.boot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.crnk.core.engine.dispatcher.RequestDispatcher;
import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.error.ErrorResponse;
import io.crnk.core.engine.error.ExceptionMapper;
import io.crnk.core.engine.error.JsonApiExceptionMapper;
import io.crnk.core.engine.filter.DocumentFilter;
import io.crnk.core.engine.information.contributor.ResourceFieldContributor;
import io.crnk.core.engine.internal.document.mapper.DocumentMapper;
import io.crnk.core.engine.internal.document.mapper.DocumentMappingConfig;
import io.crnk.core.engine.internal.repository.ResourceRepositoryAdapter;
import io.crnk.core.engine.properties.PropertiesProvider;
import io.crnk.core.engine.query.QueryAdapterBuilder;
import io.crnk.core.engine.query.QueryContext;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.engine.result.Result;
import io.crnk.core.engine.url.ConstantServiceUrlProvider;
import io.crnk.core.engine.url.ServiceUrlProvider;
import io.crnk.core.mock.MockConstants;
import io.crnk.core.mock.models.Task;
import io.crnk.core.module.Module;
import io.crnk.core.module.ModuleRegistry;
import io.crnk.core.module.SimpleModule;
import io.crnk.core.module.discovery.ReflectionsServiceDiscovery;
import io.crnk.core.module.discovery.ServiceDiscovery;
import io.crnk.core.module.discovery.ServiceDiscoveryFactory;
import io.crnk.core.queryspec.DefaultQuerySpecDeserializer;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.queryspec.QuerySpecDeserializer;
import io.crnk.core.queryspec.internal.QuerySpecAdapter;
import io.crnk.core.queryspec.internal.QuerySpecAdapterBuilder;
import io.crnk.core.queryspec.mapper.DefaultQuerySpecUrlMapper;
import io.crnk.core.queryspec.pagingspec.OffsetLimitPagingBehavior;
import io.crnk.core.repository.decorate.RepositoryDecoratorFactory;
import io.crnk.core.repository.response.JsonApiResponse;
import io.crnk.legacy.locator.JsonServiceLocator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Properties;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;

public class CrnkBootTest {

	private ServiceDiscoveryFactory serviceDiscoveryFactory;

	private ServiceDiscovery serviceDiscovery;

	@Before
	public void setup() {
		serviceDiscoveryFactory = mock(ServiceDiscoveryFactory.class);
		serviceDiscovery = mock(ServiceDiscovery.class);
		Mockito.when(serviceDiscoveryFactory.getInstance()).thenReturn(serviceDiscovery);
	}

	@Test
	public void setObjectMapper() {
		CrnkBoot boot = new CrnkBoot();
		ObjectMapper mapper = new ObjectMapper();
		boot.setObjectMapper(mapper);
		Assert.assertSame(mapper, boot.getObjectMapper());
	}

	@Test
	public void testDiscoverDeserializer() {
		CrnkBoot boot = new CrnkBoot();
		boot.setServiceDiscovery(serviceDiscovery);

		DefaultQuerySpecDeserializer instance = new DefaultQuerySpecDeserializer();
		Mockito.when(serviceDiscovery.getInstancesByType(eq(QuerySpecDeserializer.class)))
				.thenReturn(Arrays.<QuerySpecDeserializer>asList(instance));
		boot.boot();
		Assert.assertSame(instance, boot.getQuerySpecDeserializer());
	}

	@Test(expected = IllegalStateException.class)
	public void checkCannotBootTwice() {
		CrnkBoot boot = new CrnkBoot();
		boot.boot();
		boot.boot();
	}

	@Test
	public void checkCanBootOnce() {
		CrnkBoot boot = new CrnkBoot();
		boot.boot();
	}

	@Test
	public void setServiceDiscovery() {
		CrnkBoot boot = new CrnkBoot();
		ServiceDiscovery serviceDiscovery = mock(ServiceDiscovery.class);
		boot.setServiceDiscovery(serviceDiscovery);
		Assert.assertSame(serviceDiscovery, boot.getServiceDiscovery());
	}

	@Test
	public void setServiceLocator() {
		JsonServiceLocator locator = mock(JsonServiceLocator.class);
		PropertiesProvider propertiesProvider = mock(PropertiesProvider.class);
		Mockito.when(propertiesProvider.getProperty(eq(CrnkProperties.RESOURCE_SEARCH_PACKAGE))).thenReturn("a.b.c");
		CrnkBoot boot = new CrnkBoot();
		boot.setPropertiesProvider(propertiesProvider);
		boot.setServiceLocator(locator);
		boot.setServiceDiscoveryFactory(mock(ServiceDiscoveryFactory.class));
		boot.boot();

		ReflectionsServiceDiscovery serviceDiscovery = (ReflectionsServiceDiscovery) boot.getServiceDiscovery();
		Assert.assertSame(locator, serviceDiscovery.getLocator());
	}

	@Test
	public void setServiceDiscoveryFactory() {
		CrnkBoot boot = new CrnkBoot();
		boot.setServiceDiscoveryFactory(serviceDiscoveryFactory);
		boot.setDefaultServiceUrlProvider(mock(ServiceUrlProvider.class));
		boot.boot();
		Mockito.verify(serviceDiscoveryFactory, Mockito.times(1)).getInstance();
		Assert.assertNotNull(boot.getServiceDiscovery());
	}

	@Test
	public void getPropertiesProvider() {
		CrnkBoot boot = new CrnkBoot();
		boot.setServiceDiscoveryFactory(serviceDiscoveryFactory);
		boot.setDefaultServiceUrlProvider(mock(ServiceUrlProvider.class));
		boot.boot();
		Assert.assertNotNull(boot.getPropertiesProvider());
	}

	@Test
	public void setInvalidRepository() {
		SimpleModule module = new SimpleModule("test");
		module.addRepository("not a repository");
		CrnkBoot boot = new CrnkBoot();
		boot.boot();
	}

	@Test
	public void setQuerySpecDeserializer() {
		CrnkBoot boot = new CrnkBoot();
		boot.setServiceDiscoveryFactory(serviceDiscoveryFactory);
		boot.setDefaultServiceUrlProvider(mock(ServiceUrlProvider.class));

		QuerySpecDeserializer deserializer = Mockito.mock(QuerySpecDeserializer.class);
		boot.setQuerySpecDeserializer(deserializer);
		Assert.assertSame(deserializer, boot.getQuerySpecDeserializer());
		boot.boot();

		QueryAdapterBuilder queryAdapterBuilder = boot.getQueryAdapterBuilder();
		Assert.assertTrue(queryAdapterBuilder instanceof QuerySpecAdapterBuilder);
	}

	@Test
	public void testServiceDiscovery() {
		CrnkBoot boot = new CrnkBoot();
		boot.setServiceDiscoveryFactory(serviceDiscoveryFactory);
		boot.setServiceUrlProvider(mock(ServiceUrlProvider.class));

		Module module = mock(Module.class);
		RepositoryDecoratorFactory decoratorFactory = mock(RepositoryDecoratorFactory.class);
		ResourceFieldContributor resourceFieldContributor = mock(ResourceFieldContributor.class);
		DocumentFilter filter = mock(DocumentFilter.class);
		JsonApiExceptionMapper exceptionMapper = new TestExceptionMapper();
		Mockito.when(serviceDiscovery.getInstancesByType(eq(DocumentFilter.class))).thenReturn(Arrays.asList(filter));
		Mockito.when(serviceDiscovery.getInstancesByType(eq(RepositoryDecoratorFactory.class)))
				.thenReturn(Arrays.asList(decoratorFactory));
		Mockito.when(serviceDiscovery.getInstancesByType(eq(ResourceFieldContributor.class)))
				.thenReturn(Arrays.asList(resourceFieldContributor));
		Mockito.when(serviceDiscovery.getInstancesByType(eq(Module.class))).thenReturn(Arrays.asList(module));
		Mockito.when(serviceDiscovery.getInstancesByType(eq(JsonApiExceptionMapper.class)))
				.thenReturn(Arrays.asList(exceptionMapper));
		boot.boot();

		ModuleRegistry moduleRegistry = boot.getModuleRegistry();
		Assert.assertTrue(moduleRegistry.getModules().contains(module));
		Assert.assertTrue(moduleRegistry.getFilters().contains(filter));
		Assert.assertTrue(moduleRegistry.getResourceFieldContributors().contains(resourceFieldContributor));
		Assert.assertTrue(moduleRegistry.getRepositoryDecoratorFactories().contains(decoratorFactory));
		Assert.assertTrue(moduleRegistry.getExceptionMapperLookup().getExceptionMappers().contains(exceptionMapper));
	}

	class TestExceptionMapper implements ExceptionMapper<IllegalStateException> {

		@Override
		public ErrorResponse toErrorResponse(IllegalStateException exception) {
			return null;
		}

		@Override
		public IllegalStateException fromErrorResponse(ErrorResponse errorResponse) {
			return null;
		}

		@Override
		public boolean accepts(ErrorResponse errorResponse) {
			return false;
		}
	}

	@Test
	public void setDefaultServiceUrlProvider() {
		CrnkBoot boot = new CrnkBoot();
		boot.setServiceDiscoveryFactory(serviceDiscoveryFactory);
		ServiceUrlProvider serviceUrlProvider = mock(ServiceUrlProvider.class);
		boot.setDefaultServiceUrlProvider(serviceUrlProvider);
		boot.boot();
		Assert.assertEquals(serviceUrlProvider, boot.getDefaultServiceUrlProvider());
		Assert.assertEquals(serviceUrlProvider, boot.getServiceUrlProvider());
		Assert.assertEquals(serviceUrlProvider, boot.getServiceUrlProvider());
	}

	@Test
	public void setServiceUrlProvider() {
		CrnkBoot boot = new CrnkBoot();
		boot.setServiceDiscoveryFactory(serviceDiscoveryFactory);
		ServiceUrlProvider serviceUrlProvider = mock(ServiceUrlProvider.class);
		boot.setServiceUrlProvider(serviceUrlProvider);
		boot.boot();
		Assert.assertEquals(serviceUrlProvider, boot.getServiceUrlProvider());
	}

	@Test
	public void setAllowUnknownAttributes() {
		CrnkBoot boot = new CrnkBoot();
		boot.setAllowUnknownAttributes();
		boot.boot();

		DefaultQuerySpecUrlMapper urlMapper = (DefaultQuerySpecUrlMapper) boot.getUrlMapper();
		Assert.assertTrue(urlMapper.getAllowUnknownAttributes());
	}

	@Test
	public void setAllowUnknownAttributesDefaultQuerySpecDeserializer() {
		CrnkBoot boot = new CrnkBoot();
		boot.setAllowUnknownAttributes();
		boot.setQuerySpecDeserializer(new DefaultQuerySpecDeserializer());
		boot.boot();

		DefaultQuerySpecDeserializer querySpecDeserializer = (DefaultQuerySpecDeserializer) boot.getQuerySpecDeserializer();
		Assert.assertTrue(querySpecDeserializer.getAllowUnknownAttributes());
	}

	@Test
	public void setAllowUnknownParameters() {
		CrnkBoot boot = new CrnkBoot();
		boot.setAllowUnknownParameters();
		boot.boot();

		DefaultQuerySpecUrlMapper urlMapper = (DefaultQuerySpecUrlMapper) boot.getUrlMapper();
		Assert.assertTrue(urlMapper.getAllowUnknownParameters());
	}


	@Test
	public void setAllowUnknownParametersDefaultQuerySpecDeserializer() {
		CrnkBoot boot = new CrnkBoot();
		boot.setAllowUnknownParameters();
		boot.setQuerySpecDeserializer(new DefaultQuerySpecDeserializer());
		boot.boot();

		DefaultQuerySpecDeserializer querySpecDeserializer = (DefaultQuerySpecDeserializer) boot.getQuerySpecDeserializer();
		Assert.assertTrue(querySpecDeserializer.getAllowUknownParameters());
	}

	@Test
	public void setConstantServiceUrlProvider() {
		CrnkBoot boot = new CrnkBoot();
		boot.setServiceDiscoveryFactory(serviceDiscoveryFactory);
		final Properties properties = new Properties();
		properties.put(CrnkProperties.RESOURCE_DEFAULT_DOMAIN, "http://something");
		PropertiesProvider propertiesProvider = new PropertiesProvider() {

			@Override
			public String getProperty(String key) {
				return (String) properties.get(key);
			}
		};
		boot.setPropertiesProvider(propertiesProvider);
		boot.boot();

		ServiceUrlProvider serviceUrlProvider = boot.getServiceUrlProvider();
		Assert.assertTrue(serviceUrlProvider instanceof ConstantServiceUrlProvider);
		Assert.assertEquals("http://something", serviceUrlProvider.getUrl());
	}

	@Test(expected = IllegalStateException.class)
	public void testReconfigurationProtection() {
		CrnkBoot boot = new CrnkBoot();
		boot.setServiceDiscoveryFactory(serviceDiscoveryFactory);
		boot.boot();
		boot.setObjectMapper(null);
	}

	@Test
	public void boot() {
		CrnkBoot boot = new CrnkBoot();
		boot.setDefaultServiceUrlProvider(new ServiceUrlProvider() {

			@Override
			public String getUrl() {
				return "http://127.0.0.1";
			}
		});
		boot.setServiceDiscovery(new ReflectionsServiceDiscovery(MockConstants.TEST_MODELS_PACKAGE));
		boot.addModule(new SimpleModule("test"));
		boot.boot();

		QueryContext queryContext = new QueryContext();
		queryContext.setBaseUrl(boot.getServiceUrlProvider().getUrl());

		RequestDispatcher requestDispatcher = boot.getRequestDispatcher();

		ResourceRegistry resourceRegistry = boot.getResourceRegistry();
		RegistryEntry taskEntry = resourceRegistry.getEntry(Task.class);
		ResourceRepositoryAdapter repositoryAdapter = taskEntry.getResourceRepository();
		Assert.assertNotNull(repositoryAdapter.getResourceRepository());
		JsonApiResponse response = repositoryAdapter.findAll(new QuerySpecAdapter(new QuerySpec(Task.class), resourceRegistry, queryContext)).get();
		Assert.assertNotNull(response);

		Assert.assertNotNull(requestDispatcher);

		ServiceDiscovery serviceDiscovery = boot.getServiceDiscovery();
		Assert.assertNotNull(serviceDiscovery);
		Assert.assertNotNull(boot.getModuleRegistry());
		Assert.assertNotNull(boot.getExceptionMapperRegistry());

		boot.setDefaultPageLimit(20L);
		boot.setMaxPageLimit(100L);

		Assert.assertEquals(1, boot.getPagingBehaviors().size());
		Assert.assertTrue(boot.getPagingBehaviors().get(0) instanceof OffsetLimitPagingBehavior);
	}

	@Test
	public void testSetServerInfo() {
		CrnkBoot boot = new CrnkBoot();
		boot.setServiceDiscovery(new ReflectionsServiceDiscovery(MockConstants.TEST_MODELS_PACKAGE));
		boot.putServerInfo("a", "b");
		boot.boot();

		DocumentMapper documentMapper = boot.getDocumentMapper();
		DocumentMappingConfig mappingConfig = new DocumentMappingConfig();
		QuerySpecAdapter querySpecAdapter =
				new QuerySpecAdapter(new QuerySpec(Task.class), boot.getResourceRegistry(), new QueryContext());

		JsonApiResponse response = new JsonApiResponse();
		Result<Document> document = documentMapper.toDocument(response, querySpecAdapter, mappingConfig);
		ObjectNode jsonapi = document.get().getJsonapi();
		Assert.assertNotNull(jsonapi);
		Assert.assertNotNull(jsonapi.get("a"));
		Assert.assertEquals("b", jsonapi.get("a").asText());
	}

	@Test
	public void testEmptyServerInfo() {
		CrnkBoot boot = new CrnkBoot();
		boot.setServiceDiscovery(new ReflectionsServiceDiscovery(MockConstants.TEST_MODELS_PACKAGE));
		boot.boot();

		DocumentMapper documentMapper = boot.getDocumentMapper();
		DocumentMappingConfig mappingConfig = new DocumentMappingConfig();
		QuerySpecAdapter querySpecAdapter =
				new QuerySpecAdapter(new QuerySpec(Task.class), boot.getResourceRegistry(), new QueryContext());

		JsonApiResponse response = new JsonApiResponse();
		Result<Document> document = documentMapper.toDocument(response, querySpecAdapter, mappingConfig);
		ObjectNode jsonapi = document.get().getJsonapi();
		Assert.assertNull(jsonapi);
	}
}
