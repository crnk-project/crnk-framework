package io.crnk.spring.setup.boot.monitor;

import io.crnk.core.boot.CrnkBoot;
import org.springframework.boot.actuate.autoconfigure.observation.web.servlet.WebMvcObservationAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.observation.ServerRequestObservationConvention;

@Configuration
@ConditionalOnClass(WebMvcObservationAutoConfiguration.class)
@ConditionalOnProperty(prefix = "crnk.monitor.metrics", name = "enabled", havingValue = "true", matchIfMissing = true)
public class CrnkSpringActuatorMetricsAutoConfiguration {

	@Bean
	ServerRequestObservationConvention crnkWebMvcTagsProvider(CrnkBoot boot) {
		return new CrnkWebMvcTagsProvider(boot);
	}
}
