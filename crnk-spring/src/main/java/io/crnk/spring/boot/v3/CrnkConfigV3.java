package io.crnk.spring.boot.v3;

import javax.servlet.Filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.boot.CrnkProperties;
import io.crnk.core.engine.internal.jackson.JsonApiModuleBuilder;
import io.crnk.core.engine.properties.PropertiesProvider;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.engine.url.ConstantServiceUrlProvider;
import io.crnk.core.module.ModuleRegistry;
import io.crnk.spring.SpringCrnkFilter;
import io.crnk.spring.boot.CrnkSpringBootProperties;
import io.crnk.spring.internal.SpringServiceDiscovery;
import org.springframework.beans.factory.annotation.Autowired;
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
@EnableConfigurationProperties(CrnkSpringBootProperties.class)
public class CrnkConfigV3 implements ApplicationContextAware {

	private ApplicationContext applicationContext;

	@Autowired
	private CrnkSpringBootProperties properties;

	@Autowired
	private ObjectMapper objectMapper;

	@Bean
	public SpringServiceDiscovery discovery() {
		return new SpringServiceDiscovery();
	}

	@Bean
	public CrnkBoot crnkBoot(SpringServiceDiscovery serviceDiscovery) {
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
				return applicationContext.getEnvironment().getProperty(key);
			}
		});
		boot.boot();
		return boot;
	}

	@Bean
	public Filter springBootSampleCrnkFilter(CrnkBoot boot) {
		JsonApiModuleBuilder jsonApiModuleBuilder = new JsonApiModuleBuilder();
		SimpleModule parameterNamesModule = jsonApiModuleBuilder.build();

		objectMapper.registerModule(parameterNamesModule);

		return new SpringCrnkFilter(boot);
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
