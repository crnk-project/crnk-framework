package io.crnk.example.dropwizard.mongo;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.hubspot.dropwizard.guice.GuiceBundle;
import io.crnk.core.boot.CrnkProperties;
import io.crnk.example.dropwizard.mongo.domain.repository.ProjectRepository;
import io.crnk.example.dropwizard.mongo.domain.repository.TaskRepository;
import io.crnk.example.dropwizard.mongo.domain.repository.TaskToProjectRepository;
import io.crnk.example.dropwizard.mongo.managed.MongoManaged;
import io.crnk.legacy.locator.JsonServiceLocator;
import io.crnk.legacy.queryParams.DefaultQueryParamsParser;
import io.crnk.legacy.queryParams.QueryParamsBuilder;
import io.crnk.rs.CrnkFeature;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;


public class DropwizardService extends Application<DropwizardConfiguration> {

	private GuiceBundle<DropwizardConfiguration> guiceBundle;

	public static void main(String[] args) throws Exception {
		new DropwizardService().run(args);

		System.out.println("\n\nopen http://localhost:8080/projects in your browser\n\n");
	}

	@Override
	public void initialize(Bootstrap<DropwizardConfiguration> bootstrap) {

		guiceBundle = GuiceBundle.<DropwizardConfiguration>newBuilder()
				.addModule(new AbstractModule() {

					@Override
					protected void configure() {
						bind(ProjectRepository.class);
						bind(TaskRepository.class);
						bind(TaskToProjectRepository.class);
					}

					@Provides
					public MongoManaged mongoManaged(DropwizardConfiguration configuration) throws Exception {
						return new MongoManaged(configuration.mongo);
					}
				})
				.setConfigClass(DropwizardConfiguration.class)
				.build();

		bootstrap.addBundle(guiceBundle);
	}

	@Override
	public void run(DropwizardConfiguration dropwizardConfiguration, Environment environment) throws Exception {
		environment.lifecycle().manage(guiceBundle.getInjector().getInstance(MongoManaged.class));


		environment.jersey().property(CrnkProperties.RESOURCE_SEARCH_PACKAGE, "io.crnk.example.dropwizard.domain");
		environment.jersey().property(CrnkProperties.RESOURCE_DEFAULT_DOMAIN, "http://localhost:8080");

		CrnkFeature crnkFeature = new CrnkFeature(environment.getObjectMapper(),
				new QueryParamsBuilder(new DefaultQueryParamsParser()),
				new JsonServiceLocator() {
					@Override
					public <T> T getInstance(Class<T> aClass) {
						return guiceBundle.getInjector().getInstance(aClass);
					}
				});
		environment.jersey().register(crnkFeature);
	}
}
