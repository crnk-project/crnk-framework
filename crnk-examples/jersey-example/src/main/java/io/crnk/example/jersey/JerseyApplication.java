package io.crnk.example.jersey;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.core.boot.CrnkProperties;
import io.crnk.example.jersey.domain.repository.ProjectRepository;
import io.crnk.example.jersey.domain.repository.TaskRepository;
import io.crnk.example.jersey.domain.repository.TaskToProjectRepository;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.process.internal.RequestScoped;
import org.glassfish.jersey.server.ResourceConfig;

import javax.inject.Singleton;
import javax.ws.rs.ApplicationPath;

@ApplicationPath("/")
public class JerseyApplication extends ResourceConfig {

	public static final String APPLICATION_URL = "http://localhost:8080";

	public JerseyApplication() {
		property(CrnkProperties.RESOURCE_SEARCH_PACKAGE, "io.crnk.example.jersey.domain");
		property(CrnkProperties.RESOURCE_DEFAULT_DOMAIN, APPLICATION_URL);
		register(CrnkDynamicFeature.class);
		register(new AbstractBinder() {
			@Override
			public void configure() {
				bindFactory(ObjectMapperFactory.class).to(ObjectMapper.class).in(Singleton.class);
				bindService(TaskRepository.class);
				bindService(ProjectRepository.class);
				bindService(TaskToProjectRepository.class);
			}

			private void bindService(Class<?> serviceType) {
				bind(serviceType).to(serviceType).proxy(true).in(RequestScoped.class);
			}
		});

	}
}
