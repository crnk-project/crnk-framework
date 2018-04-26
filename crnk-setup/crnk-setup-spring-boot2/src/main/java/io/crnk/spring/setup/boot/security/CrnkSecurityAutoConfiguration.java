package io.crnk.spring.setup.boot.security;

import java.util.List;

import io.crnk.operations.server.OperationsModule;
import io.crnk.security.SecurityConfig;
import io.crnk.security.SecurityModule;
import io.crnk.spring.security.SpringSecurityModule;
import io.crnk.spring.setup.boot.core.CrnkCoreAutoConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @link EnableAutoConfiguration Auto-configuration} for Crnk' security module.
 * <p>
 * Activates when there is a {@link OperationsModule} on the classpath and there is no other existing
 * {@link SecurityModule} configured.
 * <p>
 * Disable with the property <code>crnk.security.enabled = false</code>
 */
@Configuration
@ConditionalOnProperty(prefix = "crnk.security", name = "enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnClass(SecurityModule.class)
@ConditionalOnMissingBean(SecurityModule.class)
@EnableConfigurationProperties({ CrnkSecurityProperties.class })
@Import({ CrnkCoreAutoConfiguration.class })
public class CrnkSecurityAutoConfiguration {

	@Autowired
	private CrnkSecurityProperties securityProperties;

	@Autowired(required = false)
	private List<SecurityModuleConfigurer> configurers;

	@Bean
	public SecurityModule securityModule() {
		SecurityConfig.Builder config = SecurityConfig.builder();
		if (configurers != null) {
			for (SecurityModuleConfigurer configurer : configurers) {
				configurer.configure(config);
			}
		}
		return SecurityModule.newServerModule(config.build());
	}

	@Bean
	public SpringSecurityModule springSecurityModule() {
		return SpringSecurityModule.create();
	}
}
