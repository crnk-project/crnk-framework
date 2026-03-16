package io.crnk.spring.setup.boot.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.boot.CrnkProperties;
import io.crnk.core.engine.properties.PropertiesProvider;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.engine.url.ConstantServiceUrlProvider;
import io.crnk.core.module.ModuleRegistry;
import io.crnk.core.module.discovery.ServiceDiscovery;
import io.crnk.core.queryspec.mapper.DefaultQuerySpecUrlMapper;
import io.crnk.core.queryspec.mapper.QuerySpecUrlMapper;
import io.crnk.core.queryspec.mapper.UrlBuilder;
import io.crnk.core.queryspec.pagingspec.OffsetLimitPagingBehavior;
import io.crnk.core.queryspec.pagingspec.OffsetLimitPagingSpec;
import io.crnk.core.queryspec.pagingspec.PagingBehavior;
import io.crnk.servlet.CrnkFilter;
import io.crnk.servlet.internal.ServletModule;
import io.crnk.spring.exception.SpringExceptionModule;
import io.crnk.spring.internal.SpringServiceDiscovery;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Current crnk configuration with JSON API compliance, QuerySpec and module support.
 * Note that there is no support for QueryParams is this version due to the lack of JSON API compatibility.
 */
@Configuration
@ConditionalOnProperty(prefix = "crnk", name = "enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnMissingBean(CrnkBoot.class)
@EnableConfigurationProperties(CrnkCoreProperties.class)
public class CrnkCoreAutoConfiguration implements ApplicationContextAware {

	private ApplicationContext applicationContext;

	private CrnkCoreProperties properties;

	private ObjectMapper objectMapper;

	@Autowired(required = false)
	private List<CrnkBootConfigurer> configurers;

	// instantiated outside of Spring to allow  easy access to UrlBuilder, ModuleRegistry, etc.
	// without introducing too many dependencies
	private SpringCrnkBoot boot = new SpringCrnkBoot();

	private SpringServiceDiscovery serviceDiscovery;

	@Autowired
	public CrnkCoreAutoConfiguration(CrnkCoreProperties properties, ObjectMapper objectMapper) {
		this.properties = properties;
		this.objectMapper = objectMapper;
		this.serviceDiscovery = new SpringServiceDiscovery();

		boot.setServiceDiscovery(serviceDiscovery);
		boot.setObjectMapper(objectMapper);

		if (properties.getDomainName() != null && properties.getPathPrefix() != null) {
			String baseUrl = properties.getDomainName() + properties.getPathPrefix();
			boot.setServiceUrlProvider(new ConstantServiceUrlProvider(baseUrl));
		}
		boot.setDefaultPageLimit(properties.getDefaultPageLimit());
		boot.setMaxPageLimit(properties.getMaxPageLimit());
		boot.setPropertiesProvider(new PropertiesProvider() {
			@Override
			public String getProperty(String key) {
				if (CrnkProperties.RESOURCE_DEFAULT_DOMAIN.equals(key)) {
					return properties.getDomainName();
				}
				if (CrnkProperties.ENFORCE_ID_NAME.equals(key)) {
					return String.valueOf(properties.isEnforceIdName());
				}
				if (CrnkProperties.WEB_PATH_PREFIX.equals(key)) {
					return properties.getPathPrefix();
				}
				if (CrnkProperties.ALLOW_UNKNOWN_ATTRIBUTES.equals(key)) {
					return String.valueOf(properties.getAllowUnknownAttributes());
				}
				if (CrnkProperties.ALLOW_UNKNOWN_PARAMETERS.equals(key)) {
					return String.valueOf(properties.getAllowUnknownParameters());
				}
				if (CrnkProperties.RETURN_404_ON_NULL.equals(key)) {
					return String.valueOf(properties.getReturn404OnNull());
				}
				return applicationContext.getEnvironment().getProperty(key);
			}
		});
		boot.addModule(new ServletModule(boot.getModuleRegistry().getHttpRequestContextProvider()));
		boot.addModule(new SpringExceptionModule());
	}

	@Bean
	public ServiceDiscovery discovery() {
		return serviceDiscovery;
	}


	public class SpringCrnkBoot extends CrnkBoot implements InitializingBean {

		@Override
		public void afterPropertiesSet() {
			// careful with this because it will scan the service discovery
			// and trigger loading many other beans. Used as initializer
			// to allow the bean construction first and avoid circular dependencies
			boot();
		}
	}

	@Bean
	public CrnkBoot crnkBoot() {
		if (configurers != null) {
			for (CrnkBootConfigurer configurer : configurers) {
				configurer.configure(boot);
			}
		}
		if (properties.getEnforceDotSeparator() != null) {
			QuerySpecUrlMapper urlMapper = boot.getUrlMapper();
			if (urlMapper instanceof DefaultQuerySpecUrlMapper) {
				((DefaultQuerySpecUrlMapper) urlMapper).setEnforceDotPathSeparator(properties.getEnforceDotSeparator());
			}
		}
		return boot;
	}

	@Bean
	@ConditionalOnMissingBean(ResourceRegistry.class)
	public ResourceRegistry crnkResourceRegistry() {
		return boot.getResourceRegistry();
	}

	@Bean
	@ConditionalOnMissingBean(UrlBuilder.class)
	public UrlBuilder crnkUrlBuilder() {
		return boot.getUrlBuilder();
	}

	@Bean
	@ConditionalOnMissingBean(ModuleRegistry.class)
	public ModuleRegistry crnkModuleRegistry() {
		return boot.getModuleRegistry();
	}

	@Bean
	@ConditionalOnMissingBean(QuerySpecUrlMapper.class)
	public QuerySpecUrlMapper querySpecUrlMapper() {
		return new DefaultQuerySpecUrlMapper();
	}

	@Bean
	@ConditionalOnMissingBean(PagingBehavior.class)
	public PagingBehavior<OffsetLimitPagingSpec> offsetLimitPagingBehavior() {
		return new OffsetLimitPagingBehavior();
	}

	@Bean
	public CrnkFilter crnkFilter(CrnkBoot boot) {
		return new CrnkFilter(boot);
	}

	@Bean
	public ResourceRegistry resourceRegistry() {
		return boot.getResourceRegistry();
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;

		this.serviceDiscovery.setApplicationContext(applicationContext);
	}
}
