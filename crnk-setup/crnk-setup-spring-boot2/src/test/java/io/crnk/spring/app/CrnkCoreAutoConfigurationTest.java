package io.crnk.spring.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.boot.CrnkProperties;
import io.crnk.core.engine.properties.PropertiesProvider;
import io.crnk.core.engine.url.ConstantServiceUrlProvider;
import io.crnk.core.queryspec.mapper.DefaultQuerySpecUrlMapper;
import io.crnk.spring.internal.SpringServiceDiscovery;
import io.crnk.spring.setup.boot.core.CrnkCoreAutoConfiguration;
import io.crnk.spring.setup.boot.core.CrnkCoreProperties;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

public class CrnkCoreAutoConfigurationTest {


    @Test
    public void checkProperties() {
        ApplicationContext applicationContext = Mockito.mock(ApplicationContext.class);
        Mockito.when(applicationContext.getEnvironment()).thenReturn(Mockito.mock(Environment.class));

        CrnkCoreProperties properties = new CrnkCoreProperties();
        properties.setDomainName("testDomain");
        properties.setDefaultPageLimit(12L);
        properties.setMaxPageLimit(20L);
        properties.setPathPrefix("/prefix");
        properties.setAllowUnknownAttributes(true);
        properties.setReturn404OnNull(true);

        ObjectMapper objectMapper = new ObjectMapper();

        CrnkCoreAutoConfiguration config = new CrnkCoreAutoConfiguration(properties, objectMapper);
        config.setApplicationContext(applicationContext);

        CrnkBoot boot = config.crnkBoot();
        boot.boot();

        PropertiesProvider propertiesProvider = boot.getPropertiesProvider();
        Assert.assertEquals("testDomain", propertiesProvider.getProperty(CrnkProperties.RESOURCE_DEFAULT_DOMAIN));
        Assert.assertEquals("/prefix", propertiesProvider.getProperty(CrnkProperties.WEB_PATH_PREFIX));
        Assert.assertEquals("true", propertiesProvider.getProperty(CrnkProperties.ALLOW_UNKNOWN_ATTRIBUTES));
        Assert.assertEquals("true", propertiesProvider.getProperty(CrnkProperties.RETURN_404_ON_NULL));

        DefaultQuerySpecUrlMapper deserializer = (DefaultQuerySpecUrlMapper) boot.getUrlMapper();
        Assert.assertTrue(deserializer.getAllowUnknownAttributes());

        ConstantServiceUrlProvider constantServiceUrlProvider = (ConstantServiceUrlProvider) boot.getServiceUrlProvider();
        Assert.assertEquals("testDomain/prefix", constantServiceUrlProvider.getUrl());

        Assert.assertSame(objectMapper, boot.getObjectMapper());

        Assert.assertNotNull(boot.getModuleRegistry().getSecurityProvider());
    }
}
