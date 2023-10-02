package io.crnk.example.jersey;

import io.crnk.core.boot.CrnkProperties;
import io.crnk.core.module.SimpleModule;
import io.crnk.example.jersey.domain.repository.ProjectRepositoryImpl;
import io.crnk.home.HomeModule;
import io.crnk.rs.CrnkFeature;
import org.glassfish.jersey.server.ResourceConfig;

import jakarta.ws.rs.ApplicationPath;

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
	}
}
