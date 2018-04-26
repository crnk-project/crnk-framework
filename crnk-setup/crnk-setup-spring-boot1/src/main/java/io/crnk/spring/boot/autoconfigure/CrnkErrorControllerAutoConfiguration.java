package io.crnk.spring.boot.autoconfigure;

import java.util.List;
import javax.servlet.Servlet;

import io.crnk.spring.boot.CrnkSpringMvcProperties;
import io.crnk.spring.mvc.CrnkErrorController;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.SearchStrategy;
import org.springframework.boot.autoconfigure.web.BasicErrorController;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.boot.autoconfigure.web.ErrorMvcAutoConfiguration;
import org.springframework.boot.autoconfigure.web.ErrorViewResolver;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.DispatcherServlet;


@Configuration
@ConditionalOnProperty(prefix = "crnk.spring.mvc", name = "errorController", havingValue = "true", matchIfMissing = true)
@ConditionalOnWebApplication
@ConditionalOnClass({ Servlet.class, DispatcherServlet.class, ErrorMvcAutoConfiguration.class })
// Load before the main ErrorMvcAutoConfiguration so that we override it
@AutoConfigureBefore(ErrorMvcAutoConfiguration.class)
@EnableConfigurationProperties({ CrnkSpringMvcProperties.class })
public class CrnkErrorControllerAutoConfiguration {

	private final ServerProperties serverProperties;

	private final List<ErrorViewResolver> errorViewResolvers;

	public CrnkErrorControllerAutoConfiguration(ServerProperties serverProperties,
			ObjectProvider<List<ErrorViewResolver>> errorViewResolversProvider) {
		this.serverProperties = serverProperties;
		this.errorViewResolvers = errorViewResolversProvider.getIfAvailable();
	}

	@Bean
	@ConditionalOnMissingBean(value = ErrorController.class, search = SearchStrategy.CURRENT)
	public BasicErrorController jsonapiErrorController(ErrorAttributes errorAttributes) {
		return new CrnkErrorController(errorAttributes, this.serverProperties.getError(), this.errorViewResolvers);
	}
}
