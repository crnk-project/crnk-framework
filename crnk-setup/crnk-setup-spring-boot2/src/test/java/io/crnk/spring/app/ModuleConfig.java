package io.crnk.spring.app;

import io.crnk.core.boot.CrnkBoot;
import io.crnk.spring.setup.boot.core.CrnkBootConfigurer;
import io.crnk.spring.setup.boot.jpa.JpaModuleConfigurer;
import io.crnk.spring.setup.boot.meta.MetaModuleConfigurer;
import io.crnk.test.mock.TestModule;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModuleConfig {

	@Bean
	public TestModule testModule() {
		return new TestModule();
	}

	@Bean
	public MetaModuleConfigurer metaModuleConfigurer() {
		return Mockito.mock(MetaModuleConfigurer.class);
	}

	@Bean
	public JpaModuleConfigurer jpaModujleConfigurer() {
		return Mockito.mock(JpaModuleConfigurer.class);
	}

	@Bean
	public CrnkBootConfigurer bootConfigurer() {
		return boot -> boot.putServerInfo("vendor", "crnk");
	}
}
