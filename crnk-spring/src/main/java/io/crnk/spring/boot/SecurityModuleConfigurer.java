package io.crnk.spring.boot;

import io.crnk.security.SecurityConfig;

public interface SecurityModuleConfigurer {

	void configure(SecurityConfig.Builder config);
}
