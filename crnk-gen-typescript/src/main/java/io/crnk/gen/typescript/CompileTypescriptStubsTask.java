package io.crnk.gen.typescript;

import com.moowork.gradle.node.npm.NpmTask;
import org.gradle.api.logging.LogLevel;

public class CompileTypescriptStubsTask extends NpmTask {

	public static final String NAME = "compileTypescript";

	public CompileTypescriptStubsTask() {
		setDescription("compiles the generated Typescript stubs");
		getLogging().setLevel(LogLevel.QUIET);

		setNpmCommand("run", "build");
	}
}