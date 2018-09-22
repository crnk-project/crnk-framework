package io.crnk.spring.setup.boot.validation;

import io.crnk.spring.setup.boot.core.CrnkCoreAutoConfiguration;
import io.crnk.validation.ValidationModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

/**
 * @link EnableAutoConfiguration Auto-configuration} for Crnk' Validation module.
 * <p>
 * Activates when there is a {@link ValidationModule} on the classpath and there is no other existing
 * {@link ValidationModule} configured.
 * <p>
 * Disable with the property <code>crnk.validation.enabled = false</code>
 */
@Configuration
@ConditionalOnProperty(prefix = "crnk.validation", name = "enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnClass(ValidationModule.class)
@ConditionalOnMissingBean(ValidationModule.class)
@EnableConfigurationProperties({CrnkValidationProperties.class})
@Import({CrnkCoreAutoConfiguration.class})
public class CrnkValidationAutoConfiguration {

	@Autowired
	private CrnkValidationProperties validationProperties;

	@Bean
	public LocalValidatorFactoryBean validatorFactoryBean() {
		final LocalValidatorFactoryBean localValidatorFactoryBean = new LocalValidatorFactoryBean();
		return localValidatorFactoryBean;
	}

	@Bean
	public ValidationModule validationModule(LocalValidatorFactoryBean validatorFactoryBean) {
		return ValidationModule.create(validationProperties.getValidateResources(), validatorFactoryBean.getValidator());
	}
}
