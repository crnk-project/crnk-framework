package io.crnk.example.jersey;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.core.boot.CrnkProperties;
import io.crnk.core.module.SimpleModule;
import io.crnk.example.jersey.domain.repository.ProjectRepositoryImpl;
import io.crnk.home.HomeModule;
import io.crnk.rs.CrnkFeature;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.process.internal.RequestScoped;
import org.glassfish.jersey.server.ResourceConfig;

import javax.inject.Singleton;
import javax.ws.rs.ApplicationPath;

@ApplicationPath("/")
public class JerseyApplication extends ResourceConfig {

	public static final String APPLICATION_URL = "http://localhost:8080";

	public JerseyApplication() {
		// in this example we have no dependency injection and nothing, so we register our implementation with a module
		SimpleModule appModule = new SimpleModule("app");
		appModule.addRepository(new ProjectRepositoryImpl());

		CrnkFeature feature = new CrnkFeature();
		feature.addModule(appModule);
		feature.addModule(HomeModule.create());

		property(CrnkProperties.RESOURCE_DEFAULT_DOMAIN, APPLICATION_URL);
		register(feature);
		register(new AbstractBinder() {
			@Override
			public void configure() {
				bindFactory(ObjectMapperFactory.class).to(ObjectMapper.class).in(Singleton.class);
				bindService(ProjectRepositoryImpl.class);
			}

			private void bindService(Class<?> serviceType) {
				bind(serviceType).to(serviceType).proxy(true).in(RequestScoped.class);
			}
		});

	}
}
