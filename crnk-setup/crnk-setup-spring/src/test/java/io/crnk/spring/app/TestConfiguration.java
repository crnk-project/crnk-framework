package io.crnk.spring.app;

import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.module.ModuleRegistry;
import io.crnk.core.module.discovery.ServiceDiscovery;
import io.crnk.core.queryspec.mapper.DefaultQuerySpecUrlMapper;
import io.crnk.core.queryspec.mapper.QuerySpecUrlMapper;
import io.crnk.core.queryspec.pagingspec.OffsetLimitPagingBehavior;
import io.crnk.core.queryspec.pagingspec.OffsetLimitPagingSpec;
import io.crnk.core.queryspec.pagingspec.PagingBehavior;
import io.crnk.servlet.CrnkFilter;
import io.crnk.servlet.internal.ServletModule;
import io.crnk.spring.internal.SpringServiceDiscovery;
import io.crnk.test.mock.TestModule;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableAutoConfiguration
public class TestConfiguration implements ApplicationContextAware {

	private ApplicationContext applicationContext;

	@Bean
	@ConditionalOnMissingBean(ServiceDiscovery.class)
	public SpringServiceDiscovery discovery() {
		return new SpringServiceDiscovery();
	}

	@Bean
	@ConditionalOnMissingBean(CrnkBoot.class)
	public CrnkBoot crnkBoot(ServiceDiscovery serviceDiscovery) {
		CrnkBoot boot = new CrnkBoot();
		boot.setServiceDiscovery(serviceDiscovery);
		boot.addModule(new ServletModule(boot.getModuleRegistry().getHttpRequestContextProvider()));
		boot.boot();
		return boot;
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
	public CrnkFilter springBootSampleCrnkFilter(CrnkBoot boot) {
		return new CrnkFilter(boot);
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

	@Bean
	public TestModule testModule() {
		return new TestModule();
	}
}
