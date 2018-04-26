package io.crnk.spring.boot;

import io.crnk.jpa.JpaModuleConfig;

public interface JpaModuleConfigurer {

	void configure(JpaModuleConfig config);
}
