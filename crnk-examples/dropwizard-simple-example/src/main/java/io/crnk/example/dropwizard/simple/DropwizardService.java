package io.crnk.example.dropwizard.simple;

import io.crnk.core.module.SimpleModule;
import io.crnk.example.dropwizard.simple.domain.repository.ProjectRepository;
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
		// here we make use of a module as example instead of using service discovery (CDI, Spring, etc.)
		// tag::docs[]
		SimpleModule module = new SimpleModule("example");
		module.addRepository(new ProjectRepository());

		CrnkFeature crnkFeature = new CrnkFeature();
		crnkFeature.addModule(module);

		environment.jersey().register(crnkFeature);
		// end::docs[]
	}
}
