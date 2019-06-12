package io.crnk.spring.boot;

import io.crnk.data.jpa.JpaModuleConfig;

public interface JpaModuleConfigurer {

	void configure(JpaModuleConfig config);
}
