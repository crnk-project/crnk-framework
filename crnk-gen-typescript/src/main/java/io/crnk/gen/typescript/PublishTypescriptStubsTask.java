package io.crnk.gen.typescript;

import com.moowork.gradle.node.npm.NpmTask;

public class PublishTypescriptStubsTask extends NpmTask {

	public static final String NAME = "publishTypescript";

	public PublishTypescriptStubsTask() {
		setGroup("publish");
		setDescription("publishes the generated Typescript stubs to a NPM repository");

		setNpmCommand("publish");
		getInputs().dir("src");
		getInputs().files("package.json");
		getOutputs().dir("dist");
	}

}