package io.crnk.example.dropwizard.simple;

import io.dropwizard.Configuration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class DropwizardConfiguration extends Configuration {

	@Valid
	@NotNull
	public CrnkConfiguration crnk = new CrnkConfiguration();

	public class CrnkConfiguration {

		@Valid
		@NotNull
		public String searchPackage = "io.crnk.example.dropwizard.simple.domain";
	}
}
