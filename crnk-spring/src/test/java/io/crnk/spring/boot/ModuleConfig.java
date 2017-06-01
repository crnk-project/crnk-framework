package io.crnk.spring.boot;

import io.crnk.spring.security.SpringSecurityModule;
import io.crnk.test.mock.TestModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModuleConfig {

	@Bean
	public TestModule testModule() {
		return new TestModule();
	}

	@Bean
	public SpringSecurityModule securityModule() {
		return  SpringSecurityModule.create();
	}
}
