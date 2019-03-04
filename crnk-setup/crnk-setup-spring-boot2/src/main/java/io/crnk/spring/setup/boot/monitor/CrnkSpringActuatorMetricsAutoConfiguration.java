package io.crnk.spring.setup.boot.monitor;

import io.crnk.core.boot.CrnkBoot;
import io.crnk.spring.setup.boot.meta.CrnkTracingProperties;
import org.springframework.boot.actuate.autoconfigure.metrics.web.servlet.WebMvcMetricsAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(WebMvcMetricsAutoConfiguration.class)
public class CrnkSpringActuatorMetricsAutoConfiguration {

	@Bean
	CrnkWebMvcTagsProvider crnkWebMvcTagsProvider(CrnkBoot boot) {
		return new CrnkWebMvcTagsProvider(boot);
	}
}
