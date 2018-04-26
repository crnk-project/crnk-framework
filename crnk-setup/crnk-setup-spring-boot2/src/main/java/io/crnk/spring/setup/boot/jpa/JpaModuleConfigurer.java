package io.crnk.spring.setup.boot.jpa;

import io.crnk.jpa.JpaModuleConfig;

public interface JpaModuleConfigurer {

	void configure(JpaModuleConfig config);
}
