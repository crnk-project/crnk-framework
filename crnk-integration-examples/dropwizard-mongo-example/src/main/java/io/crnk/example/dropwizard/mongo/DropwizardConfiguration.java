package io.crnk.example.dropwizard.mongo;

import io.dropwizard.Configuration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class DropwizardConfiguration extends Configuration {

	@Valid
	@NotNull
	public MongoConfiguration mongo = new MongoConfiguration();

}