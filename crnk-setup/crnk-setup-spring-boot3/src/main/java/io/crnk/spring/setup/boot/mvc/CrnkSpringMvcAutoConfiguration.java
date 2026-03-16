package io.crnk.spring.setup.boot.mvc;

import io.crnk.spring.setup.boot.core.CrnkCoreAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * @link EnableAutoConfiguration Auto-configuration} for Crnk' spring mvc module.
 * <p>
 * Activates when there is a {@link SpringMvcModule} on the classpath and there is no other existing
 * {@link SpringMvcModule} configured.
 * <p>
 * Disable with the property <code>crnk.spring.mvc.enabled = false</code>
 */
@Configuration
@ConditionalOnWebApplication
@ConditionalOnProperty(prefix = "crnk.spring.mvc", name = "enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnClass(RequestMappingHandlerMapping.class)
@ConditionalOnMissingBean(SpringMvcModule.class)
@EnableConfigurationProperties({CrnkSpringMvcProperties.class})
@Import({CrnkCoreAutoConfiguration.class})
public class CrnkSpringMvcAutoConfiguration {

	@Bean
	public SpringMvcModule springMvcModule() {
		return new SpringMvcModule();
	}
}
