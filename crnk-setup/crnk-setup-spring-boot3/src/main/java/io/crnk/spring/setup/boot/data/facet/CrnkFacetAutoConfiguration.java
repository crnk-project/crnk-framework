package io.crnk.spring.setup.boot.data.facet;

import io.crnk.data.facet.FacetModule;
import io.crnk.data.facet.FacetModuleConfig;
import io.crnk.spring.setup.boot.core.CrnkCoreAutoConfiguration;
import io.crnk.spring.setup.boot.core.CrnkCoreProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.List;

@Configuration
@ConditionalOnProperty(prefix = "crnk.facet", name = "enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnClass(FacetModule.class)
@ConditionalOnMissingBean(FacetModule.class)
@EnableConfigurationProperties({CrnkFacetProperties.class, CrnkCoreProperties.class})
@Import({CrnkCoreAutoConfiguration.class})
public class CrnkFacetAutoConfiguration {

	@Autowired(required = false)
	private List<FacetModuleConfigurer> configurers;

	@Bean
	@ConditionalOnMissingBean
	public FacetModuleConfig facetModuleConfig() {
		return new FacetModuleConfig();
	}

	@Bean
	@ConditionalOnMissingBean
	public FacetModule facetModule(FacetModuleConfig config) {
		if (configurers != null) {
			for (FacetModuleConfigurer configurer : configurers) {
				configurer.configure(config);
			}
		}
		return new FacetModule(config);
	}
}
