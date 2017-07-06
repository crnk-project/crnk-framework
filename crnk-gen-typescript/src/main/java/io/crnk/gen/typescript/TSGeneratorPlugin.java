package io.crnk.gen.typescript;

import java.io.File;

import com.moowork.gradle.node.npm.NpmInstallTask;
import io.crnk.gen.typescript.internal.TypescriptUtils;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.UnknownTaskException;
import org.gradle.api.artifacts.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TSGeneratorPlugin implements Plugin<Project> {

	private static final Logger LOGGER = LoggerFactory.getLogger(TSGeneratorPlugin.class);

	@Override
	public void apply(final Project project) {
		GenerateTypescriptTask generateTask = project.getTasks().create(GenerateTypescriptTask.NAME,
				GenerateTypescriptTask.class);
		CompileTypescriptStubsTask compileTypescriptTask = project.getTasks().create(CompileTypescriptStubsTask.NAME,
				CompileTypescriptStubsTask.class);

		final File workingDir = new File(project.getBuildDir(), "generated/source/typescript/");
		compileTypescriptTask.setWorkingDir(workingDir);
		compileTypescriptTask.getInputs().dir(new File(workingDir, "src"));
		compileTypescriptTask.getInputs().dir(new File(workingDir, "package.json"));
		compileTypescriptTask.getInputs().dir(new File(workingDir, ".npmrc"));
		compileTypescriptTask.getOutputs().dir(new File(project.getBuildDir(), "build/npm"));

		Configuration compileConfiguration = project.getConfigurations().getByName("compile");
		generateTask.getInputs().file(compileConfiguration.getFiles());
		generateTask.getOutputs().dir(new File(workingDir, "src"));
		generateTask.getOutputs().dir(new File(workingDir, "package.json"));
		generateTask.getOutputs().file(new File(workingDir, ".npmrc"));

		try {
			NpmInstallTask npmInstall = (NpmInstallTask) project.getTasks().getByName("npmInstall");
			npmInstall.setWorkingDir(workingDir);
			npmInstall.dependsOn(generateTask);
			npmInstall.getInputs().file(new File(workingDir, "package.json"));
			npmInstall.getOutputs().dir(new File(workingDir, "node_modules"));

			// copy .npmrc file from root to working directory if available
			final File npmrcFile = new File(project.getProjectDir(), ".npmrc");
			if (npmrcFile.exists()) {
				npmInstall.getInputs().file(npmrcFile);
				npmInstall.doFirst(new Action<Task>() {
					@Override
					public void execute(Task task) {
						File targetFile = new File(workingDir, ".npmrc");
						TypescriptUtils.copyFile(npmrcFile, targetFile);
					}
				});
			}
			compileTypescriptTask.dependsOn(npmInstall);
		}
		catch (UnknownTaskException e) {
			LOGGER.warn("task not found, ok in testing", e);
		}
		try {
			Task processIntegrationTestResourcesTask = project.getTasks().getByName("processIntegrationTestResources");
			Task integrationCompileJavaTask = project.getTasks().getByName("compileIntegrationTestJava");
			Task assembleTask = project.getTasks().getByName("assemble");

			generateTask.dependsOn(assembleTask, integrationCompileJavaTask, processIntegrationTestResourcesTask);
		}
		catch (Exception e) {
			LOGGER.error("failed to setup dependencies, is integrationTest and testSet plugin properly setup", e);
		}

		TSGeneratorConfiguration config = new TSGeneratorConfiguration();
		project.getExtensions().add("typescriptGen", config);
	}
}
