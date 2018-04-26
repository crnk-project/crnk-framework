package io.crnk.spring.boot.autoconfigure;

import java.util.List;

import io.crnk.meta.MetaModule;
import io.crnk.meta.MetaModuleConfig;
import io.crnk.meta.provider.resource.ResourceMetaProvider;
import io.crnk.spring.boot.CrnkMetaProperties;
import io.crnk.spring.boot.MetaModuleConfigurer;
import io.crnk.spring.boot.v3.CrnkConfigV3;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @link EnableAutoConfiguration Auto-configuration} for Crnk' Meta module.
 * <p>
 * Activates when there is a {@link MetaModule} on the classpath and there is no other existing
 * {@link MetaModule} configured.
 * <p>
 * Disable with the property <code>crnk.meta.enabled = false</code>
 */
@Configuration
@ConditionalOnProperty(prefix = "crnk.meta", name = "enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnClass(MetaModule.class)
@ConditionalOnMissingBean(MetaModule.class)
@EnableConfigurationProperties({ CrnkMetaProperties.class })
@Import({ CrnkConfigV3.class })
public class CrnkMetaAutoConfiguration {

	@Autowired
	private CrnkMetaProperties metaProperties;

	@Autowired(required = false)
	private List<MetaModuleConfigurer> configurers;

	@Bean
	public MetaModuleConfig metaModuleConfig() {
		MetaModuleConfig config = new MetaModuleConfig();
		if (metaProperties.getListResources()) {
			config.addMetaProvider(new ResourceMetaProvider());
		}
		return config;
	}

	@Bean
	public MetaModule metaModule(MetaModuleConfig config) {
		if (configurers != null) {
			for (MetaModuleConfigurer configurer : configurers) {
				configurer.configure(config);
			}
		}
		return MetaModule.createServerModule(config);
	}
}