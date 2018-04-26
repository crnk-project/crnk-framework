package io.crnk.spring.app;

import io.crnk.spring.boot.JpaModuleConfigurer;
import io.crnk.spring.boot.MetaModuleConfigurer;
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
	public TestSpanReporter spanReporter() {
		return new TestSpanReporter();
	}

	@Bean
	public MetaModuleConfigurer metaModuleConfigurer() {
		return Mockito.mock(MetaModuleConfigurer.class);
	}

	@Bean
	public JpaModuleConfigurer jpaModujleConfigurer() {
		return Mockito.mock(JpaModuleConfigurer.class);
	}
}
