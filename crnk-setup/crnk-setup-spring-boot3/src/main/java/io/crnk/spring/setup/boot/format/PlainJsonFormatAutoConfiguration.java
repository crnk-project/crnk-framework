package io.crnk.spring.setup.boot.format;

import io.crnk.format.plainjson.PlainJsonFormatModule;
import io.crnk.spring.setup.boot.core.CrnkCoreAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @link EnableAutoConfiguration Auto-configuration} for Crnk' Plain-Json format module.
 * <p>
 * Activates when there is a {@link PlainJsonFormatModule} on the classpath and there is no other existing
 * {@link PlainJsonFormatModule} configured.
 * <p>
 * Disable with the property <code>crnk.format.plain.enabled = false</code>
 */
@Configuration
@ConditionalOnProperty(prefix = "crnk.format.plain", name = "enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnClass(PlainJsonFormatModule.class)
@ConditionalOnMissingBean(PlainJsonFormatModule.class)
@EnableConfigurationProperties({ PlainJsonFormatProperties.class })
@Import({ CrnkCoreAutoConfiguration.class })
public class PlainJsonFormatAutoConfiguration {

	@Bean
	public PlainJsonFormatModule plainJsonFormatModule() {
		return new PlainJsonFormatModule();
	}
}
