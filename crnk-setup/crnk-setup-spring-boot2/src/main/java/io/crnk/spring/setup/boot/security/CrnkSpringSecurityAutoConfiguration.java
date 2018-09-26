package io.crnk.spring.setup.boot.security;

import io.crnk.spring.security.SpringSecurityModule;
import io.crnk.spring.setup.boot.core.CrnkCoreAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @link EnableAutoConfiguration Auto-configuration} for Crnk' spring security module.
 * <p>
 * Activates when there is are spring security classes on the classpath and there is no other existing
 * {@link SpringSecurityModule} configured.
 * <p>
 * Disable with the property <code>crnk.security.enabled = false</code>
 */
@Configuration
@ConditionalOnProperty(prefix = "crnk.security", name = "enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnClass(org.springframework.security.access.AccessDeniedException.class)
@ConditionalOnMissingBean(SpringSecurityModule.class)
@EnableConfigurationProperties({ CrnkSecurityProperties.class })
@Import({ CrnkCoreAutoConfiguration.class })
public class CrnkSpringSecurityAutoConfiguration {

	@Bean
	@ConditionalOnClass(org.springframework.security.access.AccessDeniedException.class)
	public SpringSecurityModule springSecurityModule() {
		return SpringSecurityModule.create();
	}
}
