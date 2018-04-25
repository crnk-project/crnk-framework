package io.crnk.spring.setup.boot.operations;

import io.crnk.operations.server.OperationsModule;
import io.crnk.spring.setup.boot.core.CrnkCoreAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @link EnableAutoConfiguration Auto-configuration} for Crnk' HoOperationsme module.
 * <p>
 * Activates when there is a {@link OperationsModule} on the classpath and there is no other existing
 * {@link OperationsModule} configured.
 * <p>
 * Disable with the property <code>crnk.operations.enabled = false</code>
 */
@Configuration
@ConditionalOnProperty(prefix = "crnk.operations", name = "enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnClass(OperationsModule.class)
@ConditionalOnMissingBean(OperationsModule.class)
@EnableConfigurationProperties({ CrnkOperationsProperties.class })
@Import({ CrnkCoreAutoConfiguration.class })
public class CrnkOperationsAutoConfiguration {

	@Bean
	public OperationsModule operationsModule() {
		return OperationsModule.create();
	}
}
