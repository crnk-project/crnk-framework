package io.crnk.core.boot;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.core.engine.dispatcher.RequestDispatcher;
import io.crnk.core.engine.error.ErrorResponse;
import io.crnk.core.engine.error.ExceptionMapper;
import io.crnk.core.engine.error.JsonApiExceptionMapper;
import io.crnk.core.engine.filter.DocumentFilter;
import io.crnk.core.engine.internal.http.HttpRequestProcessorImpl;
import io.crnk.core.engine.internal.repository.ResourceRepositoryAdapter;
import io.crnk.core.engine.properties.PropertiesProvider;
import io.crnk.core.engine.query.QueryAdapterBuilder;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
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
import io.crnk.core.queryspec.QuerySpecDeserializer;
import io.crnk.core.queryspec.internal.QuerySpecAdapterBuilder;
import io.crnk.core.repository.response.JsonApiResponse;
import io.crnk.legacy.internal.QueryParamsAdapter;
import io.crnk.legacy.internal.QueryParamsAdapterBuilder;
import io.crnk.legacy.locator.JsonServiceLocator;
import io.crnk.legacy.queryParams.QueryParams;
import io.crnk.legacy.queryParams.QueryParamsBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class CrnkBootTest {

	private ServiceDiscoveryFactory serviceDiscoveryFactory;

	private ServiceDiscovery serviceDiscovery;

	@Before
	public void setup() {
		serviceDiscoveryFactory = Mockito.mock(ServiceDiscoveryFactory.class);
		serviceDiscovery = Mockito.mock(ServiceDiscovery.class);
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
		Mockito.when(serviceDiscovery.getInstancesByType(Mockito.eq(QuerySpecDeserializer.class))).thenReturn(Arrays.asList
				(instance));
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
		ServiceDiscovery serviceDiscovery = Mockito.mock(ServiceDiscovery.class);
		boot.setServiceDiscovery(serviceDiscovery);
		Assert.assertSame(serviceDiscovery, boot.getServiceDiscovery());
	}

	@Test
	public void setServiceLocator() {
		JsonServiceLocator locator = Mockito.mock(JsonServiceLocator.class);
		PropertiesProvider propertiesProvider = Mockito.mock(PropertiesProvider.class);
		Mockito.when(propertiesProvider.getProperty(Mockito.eq(CrnkProperties.RESOURCE_SEARCH_PACKAGE))).thenReturn("a.b.c");
		CrnkBoot boot = new CrnkBoot();
		boot.setPropertiesProvider(propertiesProvider);
		boot.setServiceLocator(locator);
		boot.setServiceDiscoveryFactory(Mockito.mock(ServiceDiscoveryFactory.class));
		boot.boot();

		ReflectionsServiceDiscovery serviceDiscovery = (ReflectionsServiceDiscovery) boot.getServiceDiscovery();
		Assert.assertSame(locator, serviceDiscovery.getLocator());
	}

	@Test
	public void setServiceDiscoveryFactory() {
		CrnkBoot boot = new CrnkBoot();
		boot.setServiceDiscoveryFactory(serviceDiscoveryFactory);
		boot.setDefaultServiceUrlProvider(Mockito.mock(ServiceUrlProvider.class));
		boot.boot();
		Mockito.verify(serviceDiscoveryFactory, Mockito.times(1)).getInstance();
		Assert.assertNotNull(boot.getServiceDiscovery());
	}

	@Test
	public void getPropertiesProvider() {
		CrnkBoot boot = new CrnkBoot();
		boot.setServiceDiscoveryFactory(serviceDiscoveryFactory);
		boot.setDefaultServiceUrlProvider(Mockito.mock(ServiceUrlProvider.class));
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
		boot.setDefaultServiceUrlProvider(Mockito.mock(ServiceUrlProvider.class));

		QuerySpecDeserializer deserializer = Mockito.mock(QuerySpecDeserializer.class);
		boot.setQuerySpecDeserializer(deserializer);
		Assert.assertSame(deserializer, boot.getQuerySpecDeserializer());
		boot.boot();

		HttpRequestProcessorImpl requestDispatcher = boot.getRequestDispatcher();
		QueryAdapterBuilder queryAdapterBuilder = requestDispatcher.getQueryAdapterBuilder();
		Assert.assertTrue(queryAdapterBuilder instanceof QuerySpecAdapterBuilder);
	}

	@Test
	public void setQueryParamsBuilder() {
		CrnkBoot boot = new CrnkBoot();
		boot.setServiceDiscoveryFactory(serviceDiscoveryFactory);
		boot.setDefaultServiceUrlProvider(Mockito.mock(ServiceUrlProvider.class));

		QueryParamsBuilder deserializer = Mockito.mock(QueryParamsBuilder.class);
		boot.setQueryParamsBuilds(deserializer);
		boot.boot();

		HttpRequestProcessorImpl requestDispatcher = boot.getRequestDispatcher();
		QueryAdapterBuilder queryAdapterBuilder = requestDispatcher.getQueryAdapterBuilder();
		Assert.assertTrue(queryAdapterBuilder instanceof QueryParamsAdapterBuilder);
	}


	@Test(expected = IllegalStateException.class)
	public void setQueryParamsBuilderErrorsWhenSettingMaxPage() {
		CrnkBoot boot = new CrnkBoot();
		QueryParamsBuilder deserializer = Mockito.mock(QueryParamsBuilder.class);
		boot.setQueryParamsBuilds(deserializer);
		boot.setMaxPageLimit(10L);
	}

	@Test(expected = IllegalStateException.class)
	public void setQueryParamsBuilderErrorsWhenSettingDefaultPage() {
		CrnkBoot boot = new CrnkBoot();
		QueryParamsBuilder deserializer = Mockito.mock(QueryParamsBuilder.class);
		boot.setQueryParamsBuilds(deserializer);
		boot.setDefaultPageLimit(10L);
	}

	@Test
	public void testServiceDiscovery() {
		CrnkBoot boot = new CrnkBoot();
		boot.setServiceDiscoveryFactory(serviceDiscoveryFactory);
		boot.setServiceUrlProvider(Mockito.mock(ServiceUrlProvider.class));

		Module module = Mockito.mock(Module.class);
		DocumentFilter filter = Mockito.mock(DocumentFilter.class);
		JsonApiExceptionMapper exceptionMapper = new TestExceptionMapper();
		Mockito.when(serviceDiscovery.getInstancesByType(Mockito.eq(DocumentFilter.class))).thenReturn(Arrays.asList(filter));
		Mockito.when(serviceDiscovery.getInstancesByType(Mockito.eq(Module.class))).thenReturn(Arrays.asList(module));
		Mockito.when(serviceDiscovery.getInstancesByType(Mockito.eq(JsonApiExceptionMapper.class)))
				.thenReturn(Arrays.asList(exceptionMapper));
		boot.boot();

		ModuleRegistry moduleRegistry = boot.getModuleRegistry();
		Assert.assertTrue(moduleRegistry.getModules().contains(module));
		Assert.assertTrue(moduleRegistry.getFilters().contains(filter));
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
		ServiceUrlProvider serviceUrlProvider = Mockito.mock(ServiceUrlProvider.class);
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
		ServiceUrlProvider serviceUrlProvider = Mockito.mock(ServiceUrlProvider.class);
		boot.setServiceUrlProvider(serviceUrlProvider);
		boot.boot();
		Assert.assertEquals(serviceUrlProvider, boot.getServiceUrlProvider());
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

		RequestDispatcher requestDispatcher = boot.getRequestDispatcher();

		ResourceRegistry resourceRegistry = boot.getResourceRegistry();
		RegistryEntry taskEntry = resourceRegistry.getEntry(Task.class);
		Assert.assertNotEquals(0, taskEntry.getRelationshipEntries().size());
		ResourceRepositoryAdapter<?, ?> repositoryAdapter = taskEntry.getResourceRepository(null);
		Assert.assertNotNull(repositoryAdapter.getResourceRepository());
		JsonApiResponse response = repositoryAdapter
				.findAll(new QueryParamsAdapter(taskEntry.getResourceInformation(), new QueryParams(), boot.getModuleRegistry
						()));
		Assert.assertNotNull(response);

		Assert.assertNotNull(requestDispatcher);

		ServiceDiscovery serviceDiscovery = boot.getServiceDiscovery();
		Assert.assertNotNull(serviceDiscovery);
		Assert.assertNotNull(boot.getModuleRegistry());
		Assert.assertNotNull(boot.getExceptionMapperRegistry());

		List<Module> modules = boot.getModuleRegistry().getModules();
		Assert.assertEquals(4, modules.size());
		boot.setDefaultPageLimit(20L);
		boot.setMaxPageLimit(100L);
	}
}
