package io.crnk.spring.boot.autoconfigure;

import io.crnk.spring.boot.CrnkSpringCloudSleuthProperties;
import io.crnk.spring.boot.CrnkSpringMvcProperties;
import io.crnk.spring.boot.v3.CrnkConfigV3;
import io.crnk.spring.cloud.sleuth.SpringCloudSleuthModule;
import io.crnk.spring.mvc.SpringMvcModule;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @link EnableAutoConfiguration Auto-configuration} for Crnk' spring cloud sleuth module.
 * <p>
 * Activates when there is a {@link SpringCloudSleuthModule} on the classpath and there is no other existing
 * {@link SpringCloudSleuthModule} configured.
 * <p>
 * Disable with the property <code>crnk.spring.mvc.enabled = false</code>
 */
@Configuration
@ConditionalOnProperty(prefix = "crnk.spring.sleuth", name = "enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnBean(Tracer.class)
@ConditionalOnMissingBean(SpringCloudSleuthModule.class)

@EnableConfigurationProperties({ CrnkSpringCloudSleuthProperties.class })
@Import({ CrnkConfigV3.class })
public class CrnkSpringCloudSleuthAutoConfiguration {

	@Bean
	public SpringCloudSleuthModule springCloudSleuthModule() {
		return new SpringCloudSleuthModule();
	}
}
