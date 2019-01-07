package io.crnk.spring.setup.boot.metrics;

import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.spring.setup.boot.metrics.CrnkWebMvcTagsProvider;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.actuate.autoconfigure.metrics.web.servlet.WebMvcMetricsAutoConfiguration;
import org.springframework.boot.actuate.metrics.web.servlet.WebMvcMetricsFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(WebMvcMetricsAutoConfiguration.class)
public class CrnkSpringActuatorMetricsAutoConfiguration {

	@Bean
	CrnkWebMvcTagsProvider crnkWebMvcTagsProvider(final ResourceRegistry resourceRegistry) {
		return new CrnkWebMvcTagsProvider(resourceRegistry);
	}
}
