package io.crnk.spring.setup.boot.jpa;

import io.crnk.data.jpa.JpaModuleConfig;

public interface JpaModuleConfigurer {

	void configure(JpaModuleConfig config);
}
