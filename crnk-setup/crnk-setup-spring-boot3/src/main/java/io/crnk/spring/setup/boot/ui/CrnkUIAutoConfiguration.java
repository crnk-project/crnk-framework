package io.crnk.spring.setup.boot.ui;

import io.crnk.spring.setup.boot.core.CrnkCoreAutoConfiguration;
import io.crnk.ui.UIModule;
import io.crnk.ui.UIModuleConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @link EnableAutoConfiguration Auto-configuration} for Crnk' UI module.
 * <p>
 * Activates when there is a {@link UIModule} on the classpath and there is no other existing
 * {@link UIModule} configured.
 * <p>
 * Disable with the property <code>crnk.ui.enabled = false</code>
 */
@Configuration
@ConditionalOnProperty(prefix = "crnk.ui", name = "enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnClass(UIModule.class)
@ConditionalOnMissingBean(UIModule.class)
@EnableConfigurationProperties({ CrnkUiProperties.class })
@Import({ CrnkCoreAutoConfiguration.class })
public class CrnkUIAutoConfiguration {

	@Bean
	public UIModule uiModule(CrnkUiProperties properties) {
		UIModuleConfig config = new UIModuleConfig();
		config.setBrowserEnabled(properties.isBrowserEnabled());
		return UIModule.create(config);
	}
}
