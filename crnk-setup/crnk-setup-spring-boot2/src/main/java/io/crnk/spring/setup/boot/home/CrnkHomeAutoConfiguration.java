package io.crnk.spring.setup.boot.home;

import io.crnk.home.HomeModule;
import io.crnk.spring.setup.boot.core.CrnkCoreAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @link EnableAutoConfiguration Auto-configuration} for Crnk' Home module.
 * <p>
 * Activates when there is a {@link HomeModule} on the classpath and there is no other existing
 * {@link HomeModule} configured.
 * <p>
 * Disable with the property <code>crnk.home.enabled = false</code>
 */
@Configuration
@ConditionalOnProperty(prefix = "crnk.home", name = "enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnClass(HomeModule.class)
@ConditionalOnMissingBean(HomeModule.class)
@EnableConfigurationProperties({CrnkHomeProperties.class})
@Import({CrnkCoreAutoConfiguration.class})
public class CrnkHomeAutoConfiguration {

	@Bean
	public HomeModule homeModule() {
		return HomeModule.create();
	}
}
