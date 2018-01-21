package io.crnk.spring.boot.v3;

import javax.servlet.Filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.boot.CrnkProperties;
import io.crnk.core.engine.properties.PropertiesProvider;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.engine.url.ConstantServiceUrlProvider;
import io.crnk.core.module.ModuleRegistry;
import io.crnk.core.module.discovery.ServiceDiscovery;
import io.crnk.core.queryspec.DefaultQuerySpecDeserializer;
import io.crnk.core.queryspec.QuerySpecDeserializer;
import io.crnk.servlet.internal.ServletModule;
import io.crnk.spring.SpringCrnkFilter;
import io.crnk.spring.boot.CrnkSpringBootProperties;
import io.crnk.spring.internal.SpringServiceDiscovery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Current crnk configuration with JSON API compliance, QuerySpec and module support.
 * Note that there is no support for QueryParams is this version due to the lack of JSON API compatibility.
 */
@Configuration
@ConditionalOnProperty(prefix = "crnk", name = "enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnMissingBean(CrnkBoot.class)
@EnableConfigurationProperties(CrnkSpringBootProperties.class)
public class CrnkConfigV3 implements ApplicationContextAware {

	private ApplicationContext applicationContext;

	private CrnkSpringBootProperties properties;

	private ObjectMapper objectMapper;

	@Autowired
	public CrnkConfigV3(CrnkSpringBootProperties properties, ObjectMapper objectMapper) {
		this.properties = properties;
		this.objectMapper = objectMapper;
	}

	@Bean
	@ConditionalOnMissingBean(ServiceDiscovery.class)
	public SpringServiceDiscovery discovery() {
		return new SpringServiceDiscovery();
	}

	@Bean
	@ConditionalOnMissingBean(CrnkBoot.class)
	public CrnkBoot crnkBoot(ServiceDiscovery serviceDiscovery) {
		CrnkBoot boot = new CrnkBoot();
		boot.setObjectMapper(objectMapper);

		if (properties.getDomainName() != null && properties.getPathPrefix() != null) {
			String baseUrl = properties.getDomainName() + properties.getPathPrefix();
			boot.setServiceUrlProvider(new ConstantServiceUrlProvider(baseUrl));
		}
		boot.setServiceDiscovery(serviceDiscovery);
		boot.setDefaultPageLimit(properties.getDefaultPageLimit());
		boot.setMaxPageLimit(properties.getMaxPageLimit());
		boot.setPropertiesProvider(new PropertiesProvider() {
			@Override
			public String getProperty(String key) {
				if (CrnkProperties.RESOURCE_SEARCH_PACKAGE.equals(key)) {
					return properties.getResourcePackage();
				}
				if (CrnkProperties.RESOURCE_DEFAULT_DOMAIN.equals(key)) {
					return properties.getDomainName();
				}
				if (CrnkProperties.WEB_PATH_PREFIX.equals(key)) {
					return properties.getPathPrefix();
				}
				if (CrnkProperties.ALLOW_UNKNOWN_ATTRIBUTES.equals(key)) {
					return String.valueOf(properties.getAllowUnknownAttributes());
				}
				if (CrnkProperties.RETURN_404_ON_NULL.equals(key)) {
					return String.valueOf(properties.getReturn404OnNull());
				}
				return applicationContext.getEnvironment().getProperty(key);
			}
		});
		boot.setAllowUnknownAttributes();
		boot.addModule(new ServletModule(boot.getModuleRegistry().getHttpRequestContextProvider()));
		boot.boot();
		return boot;
	}

	@Bean
	@ConditionalOnMissingBean(QuerySpecDeserializer.class)
	public QuerySpecDeserializer querySpecDeserializer() {
		return new DefaultQuerySpecDeserializer();
	}

	@Bean
	public SpringCrnkFilter springBootSampleCrnkFilter(CrnkBoot boot) {
		return new SpringCrnkFilter(boot, properties);
	}

	@Bean
	public ResourceRegistry resourceRegistry(CrnkBoot boot) {
		return boot.getResourceRegistry();
	}

	@Bean
	public ModuleRegistry moduleRegistry(CrnkBoot boot) {
		return boot.getModuleRegistry();
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}
}
