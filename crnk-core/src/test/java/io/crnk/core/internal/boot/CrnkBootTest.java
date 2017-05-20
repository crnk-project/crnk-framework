package io.crnk.core.internal.boot;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.boot.CrnkProperties;
import io.crnk.core.engine.dispatcher.RequestDispatcher;
import io.crnk.core.engine.error.JsonApiExceptionMapper;
import io.crnk.core.engine.filter.DocumentFilter;
import io.crnk.core.engine.information.resource.ResourceFieldNameTransformer;
import io.crnk.core.engine.internal.dispatcher.HttpRequestProcessorImpl;
import io.crnk.core.engine.internal.repository.ResourceRepositoryAdapter;
import io.crnk.core.engine.properties.PropertiesProvider;
import io.crnk.core.engine.query.QueryAdapterBuilder;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.engine.url.ConstantServiceUrlProvider;
import io.crnk.core.engine.url.ServiceUrlProvider;
import io.crnk.core.mock.models.Task;
import io.crnk.core.module.Module;
import io.crnk.core.module.ModuleRegistry;
import io.crnk.core.module.SimpleModule;
import io.crnk.core.module.discovery.ServiceDiscovery;
import io.crnk.core.module.discovery.ServiceDiscoveryFactory;
import io.crnk.core.queryspec.QuerySpecDeserializer;
import io.crnk.core.queryspec.internal.QuerySpecAdapterBuilder;
import io.crnk.core.repository.response.JsonApiResponse;
import io.crnk.core.resource.registry.ResourceRegistryBuilderTest;
import io.crnk.legacy.internal.QueryParamsAdapter;
import io.crnk.legacy.internal.QueryParamsAdapterBuilder;
import io.crnk.legacy.locator.SampleJsonServiceLocator;
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
	public void setServiceDiscovery() {
		CrnkBoot boot = new CrnkBoot();
		ServiceDiscovery serviceDiscovery = Mockito.mock(ServiceDiscovery.class);
		boot.setServiceDiscovery(serviceDiscovery);
		Assert.assertSame(serviceDiscovery, boot.getServiceDiscovery());
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
		JsonApiExceptionMapper exceptionMapper = Mockito.mock(JsonApiExceptionMapper.class);
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

	@Test
	public void setDefaultServiceUrlProvider() {
		CrnkBoot boot = new CrnkBoot();
		boot.setServiceDiscoveryFactory(serviceDiscoveryFactory);
		ServiceUrlProvider serviceUrlProvider = Mockito.mock(ServiceUrlProvider.class);
		boot.setDefaultServiceUrlProvider(serviceUrlProvider);
		boot.boot();
		Assert.assertEquals(serviceUrlProvider, boot.getResourceRegistry().getServiceUrlProvider());
	}

	@Test
	public void setServiceUrlProvider() {
		CrnkBoot boot = new CrnkBoot();
		boot.setServiceDiscoveryFactory(serviceDiscoveryFactory);
		ServiceUrlProvider serviceUrlProvider = Mockito.mock(ServiceUrlProvider.class);
		boot.setServiceUrlProvider(serviceUrlProvider);
		boot.boot();
		Assert.assertEquals(serviceUrlProvider, boot.getResourceRegistry().getServiceUrlProvider());
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

		ServiceUrlProvider serviceUrlProvider = boot.getResourceRegistry().getServiceUrlProvider();
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
		ObjectMapper objectMapper = boot.getObjectMapper();
		ResourceFieldNameTransformer resourceFieldNameTransformer = new ResourceFieldNameTransformer(
				objectMapper.getSerializationConfig());

		final Properties properties = new Properties();
		properties.put(CrnkProperties.RESOURCE_SEARCH_PACKAGE, ResourceRegistryBuilderTest.TEST_MODELS_PACKAGE);
		PropertiesProvider propertiesProvider = new PropertiesProvider() {

			@Override
			public String getProperty(String key) {
				return (String) properties.get(key);
			}
		};

		boot.setServiceLocator(new SampleJsonServiceLocator());
		boot.setDefaultServiceUrlProvider(new ServiceUrlProvider() {

			@Override
			public String getUrl() {
				return "http://127.0.0.1";
			}
		});
		boot.setPropertiesProvider(propertiesProvider);
		boot.setResourceFieldNameTransformer(resourceFieldNameTransformer);
		boot.addModule(new SimpleModule("test"));
		boot.boot();

		RequestDispatcher requestDispatcher = boot.getRequestDispatcher();

		ResourceRegistry resourceRegistry = boot.getResourceRegistry();
		RegistryEntry taskEntry = resourceRegistry.findEntry(Task.class);
		Assert.assertNotEquals(0, taskEntry.getRelationshipEntries().size());
		ResourceRepositoryAdapter<?, ?> repositoryAdapter = taskEntry.getResourceRepository(null);
		Assert.assertNotNull(repositoryAdapter.getResourceRepository());
		JsonApiResponse response = repositoryAdapter.findAll(new QueryParamsAdapter(new QueryParams()));
		Assert.assertNotNull(response);

		Assert.assertNotNull(requestDispatcher);

		ServiceDiscovery serviceDiscovery = boot.getServiceDiscovery();
		Assert.assertNotNull(serviceDiscovery);
		Assert.assertNotNull(boot.getModuleRegistry());
		Assert.assertNotNull(boot.getExceptionMapperRegistry());

		List<Module> modules = boot.getModuleRegistry().getModules();
		Assert.assertEquals(2, modules.size());
		boot.setDefaultPageLimit(20L);
		boot.setMaxPageLimit(100L);
	}
}
