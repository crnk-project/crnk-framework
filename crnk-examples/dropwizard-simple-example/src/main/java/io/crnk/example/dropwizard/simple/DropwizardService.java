package io.crnk.example.dropwizard.simple;

import io.crnk.core.boot.CrnkProperties;
import io.crnk.rs.CrnkFeature;
import io.dropwizard.Application;
import io.dropwizard.setup.Environment;

public class DropwizardService extends Application<DropwizardConfiguration> {

	public static void main(String[] args) throws Exception {
		new DropwizardService().run("server");

		System.out.println("\n\nopen http://localhost:8080/projects in your browser\n\n");
	}

	@Override
	public void run(DropwizardConfiguration dropwizardConfiguration, Environment environment) throws Exception {
		environment.jersey().property(CrnkProperties.RESOURCE_SEARCH_PACKAGE, dropwizardConfiguration.crnk.searchPackage);

		CrnkFeature crnkFeature = new CrnkFeature();
		environment.jersey().register(crnkFeature);
	}
}
