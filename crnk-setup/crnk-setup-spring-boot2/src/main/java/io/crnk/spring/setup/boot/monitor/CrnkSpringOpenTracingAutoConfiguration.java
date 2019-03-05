package io.crnk.spring.setup.boot.monitor;

import io.crnk.monitor.opentracing.OpenTracingServerModule;
import io.crnk.spring.setup.boot.meta.CrnkTracingProperties;
import io.opentracing.Tracer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(OpenTracingServerModule.class)
@EnableConfigurationProperties(CrnkTracingProperties.class)
@ConditionalOnProperty(prefix = "crnk.monitor.tracing", name = "enabled", havingValue = "true", matchIfMissing = true)
public class CrnkSpringOpenTracingAutoConfiguration {

	@Bean
	@ConditionalOnBean(Tracer.class)
	@ConditionalOnMissingBean(OpenTracingServerModule.class)
	OpenTracingServerModule crnkOpenTracingServerModule(Tracer tracer) {
		return new OpenTracingServerModule(tracer);
	}
}
