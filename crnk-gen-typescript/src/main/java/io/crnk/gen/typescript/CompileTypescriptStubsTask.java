package io.crnk.gen.typescript;

import java.io.File;

import com.moowork.gradle.node.npm.NpmTask;
import org.gradle.api.logging.LogLevel;

public class CompileTypescriptStubsTask extends NpmTask {

	public static final String NAME = "compileTypescript";

	public CompileTypescriptStubsTask() {
		setDescription("compiles the generated Typescript stubs");
		getLogging().setLevel(LogLevel.QUIET);

		setNpmCommand("run", "build");
		getOutputs().dir("dist/npm");
	}

	public void setWorkingDir(File workingDir) {
		getInputs().dir(new File(workingDir, "src"));
		getInputs().dir(new File(workingDir, "package.json"));

		super.setWorkingDir(workingDir);
	}

}