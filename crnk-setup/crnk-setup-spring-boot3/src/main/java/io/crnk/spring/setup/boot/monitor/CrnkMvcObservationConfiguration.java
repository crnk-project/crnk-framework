package io.crnk.spring.setup.boot.monitor;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "crnk.monitor.metrics", name = "enabled", havingValue = "true", matchIfMissing = true)
public class CrnkMvcObservationConfiguration {
	@Bean
	public CrnkServerRequestObservationConvention extendedServerRequestObservationConvention() {
		return new CrnkServerRequestObservationConvention();
	}
}
