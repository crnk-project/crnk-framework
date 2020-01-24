package io.crnk.spring.setup.boot.core;

import io.crnk.core.engine.internal.utils.PropertyUtils;
import org.apache.catalina.connector.Connector;
import org.apache.coyote.ProtocolHandler;
import org.apache.coyote.http11.AbstractHttp11Protocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.web.embedded.tomcat.ConfigurableTomcatWebServerFactory;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * See https://stackoverflow.com/questions/41053653/tomcat-8-is-not-able-to-handle-get-request-with-in-query-parameters
 * /44005213#44005213.
 * Relaxes tomcat connector for [ and ] till browser vendor properly implement spec (if they ever do so).
 */
@Configuration
@ConditionalOnClass({Connector.class, AbstractHttp11Protocol.class})
public class CrnkTomcatAutoConfiguration {

	private static final Logger LOGGER = LoggerFactory.getLogger(CrnkTomcatAutoConfiguration.class);

	private static final String PROPERTY_NAME = "relaxedQueryChars";

	@Bean
	public CrnkWebServerFactoryCustomizer tomcatCustomizer() {
		return new CrnkWebServerFactoryCustomizer();
	}

	class CrnkTomcatCustomizer implements TomcatConnectorCustomizer {

		@Override
		public void customize(Connector connector) {
			ProtocolHandler protocolHandler = connector.getProtocolHandler();
			if (protocolHandler instanceof AbstractHttp11Protocol) {
				AbstractHttp11Protocol protocol11 = (AbstractHttp11Protocol) protocolHandler;
				try {
					String relaxedQueryChars = (String) PropertyUtils.getProperty(protocol11, PROPERTY_NAME);
					if (relaxedQueryChars == null) {
						relaxedQueryChars = "";
					}
					relaxedQueryChars += "[]{}";
					PropertyUtils.setProperty(protocol11, PROPERTY_NAME, relaxedQueryChars);
				} catch (Exception e) {
					LOGGER.debug("failed to set relaxed query charts, Tomcat might be outdated");
				}
			}
		}
	}


	public class CrnkWebServerFactoryCustomizer implements
			WebServerFactoryCustomizer<ConfigurableTomcatWebServerFactory>, Ordered {

		@Override
		public int getOrder() {
			return 0;
		}

		@Override
		public void customize(ConfigurableTomcatWebServerFactory factory) {
			factory.addConnectorCustomizers(new CrnkTomcatCustomizer());
		}
	}
}
