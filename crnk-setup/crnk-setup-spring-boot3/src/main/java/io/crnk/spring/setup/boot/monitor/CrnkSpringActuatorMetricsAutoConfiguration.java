package io.crnk.spring.setup.boot.monitor;

import io.crnk.core.boot.CrnkBoot;
//import org.springframework.boot.actuate.autoconfigure.metrics.web.servlet.WebMvcMetricsAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
//@ConditionalOnClass(WebMvcMetricsAutoConfiguration.class)
@ConditionalOnProperty(prefix = "crnk.monitor.metrics", name = "enabled", havingValue = "true", matchIfMissing = true)
public class CrnkSpringActuatorMetricsAutoConfiguration {

	@Bean
	CrnkWebMvcTagsProvider crnkWebMvcTagsProvider(CrnkBoot boot) {
		return new CrnkWebMvcTagsProvider(boot);
	}
}
