package io.crnk.spring.setup.boot.security;

import io.crnk.security.SecurityConfig;

public interface SecurityModuleConfigurer {

	void configure(SecurityConfig.Builder config);
}
