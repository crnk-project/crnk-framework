package io.crnk.spring.boot;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.boot.CrnkProperties;
import io.crnk.core.engine.properties.PropertiesProvider;
import io.crnk.core.engine.url.ConstantServiceUrlProvider;
import io.crnk.core.queryspec.DefaultQuerySpecDeserializer;
import io.crnk.spring.boot.v3.CrnkConfigV3;
import io.crnk.spring.internal.SpringServiceDiscovery;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

public class CrnkConfigV3Test {


	@Test
	public void checkProperties() {
		ApplicationContext applicationContext = Mockito.mock(ApplicationContext.class);
		Mockito.when(applicationContext.getEnvironment()).thenReturn(Mockito.mock(Environment.class));

		CrnkSpringBootProperties properties = new CrnkSpringBootProperties();
		properties.setDomainName("testDomain");
		properties.setDefaultPageLimit(12L);
		properties.setMaxPageLimit(20L);
		properties.setPathPrefix("prefix");
		properties.setAllowUnknownAttributes(true);
		properties.setReturn404OnNull(true);
		properties.setResourcePackage("ch.something");

		ObjectMapper objectMapper = new ObjectMapper();

		CrnkConfigV3 config = new CrnkConfigV3(properties, objectMapper);
		config.setApplicationContext(applicationContext);

		SpringServiceDiscovery serviceDiscovery = Mockito.mock(SpringServiceDiscovery.class);
		CrnkBoot boot = config.crnkBoot(serviceDiscovery);

		PropertiesProvider propertiesProvider = boot.getPropertiesProvider();
		Assert.assertEquals("testDomain", propertiesProvider.getProperty(CrnkProperties.RESOURCE_DEFAULT_DOMAIN));
		Assert.assertEquals("ch.something", propertiesProvider.getProperty(CrnkProperties.RESOURCE_SEARCH_PACKAGE));
		Assert.assertEquals("prefix", propertiesProvider.getProperty(CrnkProperties.WEB_PATH_PREFIX));
		Assert.assertEquals("true", propertiesProvider.getProperty(CrnkProperties.ALLOW_UNKNOWN_ATTRIBUTES));
		Assert.assertEquals("true", propertiesProvider.getProperty(CrnkProperties.RETURN_404_ON_NULL));

		DefaultQuerySpecDeserializer deserializer = (DefaultQuerySpecDeserializer) boot.getQuerySpecDeserializer();
		Assert.assertEquals(12L, deserializer.getDefaultLimit().longValue());
		Assert.assertEquals(20L, deserializer.getMaxPageLimit().longValue());
		Assert.assertTrue(deserializer.getAllowUnknownAttributes());

		ConstantServiceUrlProvider constantServiceUrlProvider = (ConstantServiceUrlProvider) boot.getServiceUrlProvider();
		Assert.assertEquals("testDomainprefix", constantServiceUrlProvider.getUrl());

		Assert.assertSame(objectMapper, boot.getObjectMapper());
	}
}
