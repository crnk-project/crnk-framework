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
import org.gradle.api.file.ConfigurableFileTree;
import org.gradle.api.tasks.Copy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TSGeneratorPlugin implements Plugin<Project> {

	private static final Logger LOGGER = LoggerFactory.getLogger(TSGeneratorPlugin.class);

	@Override
	public void apply(final Project project) {
		final File sourcesDir = new File(project.getBuildDir(), "generated/source/typescript/");
		final File buildDir = new File(project.getBuildDir(), "npm_compile");
		final File distDir = new File(project.getBuildDir(), "npm");

		Configuration compileConfiguration = project.getConfigurations().getByName("compile");
		GenerateTypescriptTask generateTask = project.getTasks().create(GenerateTypescriptTask.NAME,
				GenerateTypescriptTask.class);
		generateTask.getInputs().file(compileConfiguration.getFiles());
		generateTask.getOutputs().dir(sourcesDir);

		Copy copySources = project.getTasks().create("processTypescript", Copy.class);
		copySources.from(sourcesDir);
		copySources.into(buildDir);
		copySources.dependsOn(generateTask);

		// copy .npmrc file from root to working directory if available
		final File npmrcFile = new File(project.getProjectDir(), ".npmrc");
		if (npmrcFile.exists()) {
			copySources.getInputs().file(npmrcFile);
			copySources.doFirst(new Action<Task>() {
				@Override
				public void execute(Task task) {
					File targetFile = new File(buildDir, ".npmrc");
					TypescriptUtils.copyFile(npmrcFile, targetFile);
				}
			});
		}

		CompileTypescriptStubsTask compileTypescriptTask = project.getTasks().create(CompileTypescriptStubsTask.NAME,
				CompileTypescriptStubsTask.class);
		try {
			NpmInstallTask npmInstall = (NpmInstallTask) project.getTasks().getByName("npmInstall");
			npmInstall.setWorkingDir(buildDir);
			npmInstall.dependsOn(copySources);
			npmInstall.getInputs().file(new File(buildDir, "package.json"));
			npmInstall.getOutputs().dir(new File(buildDir, "node_modules"));
			compileTypescriptTask.dependsOn(npmInstall);
		}
		catch (UnknownTaskException e) {
			LOGGER.warn("task not found, ok in testing", e);
		}

		ConfigurableFileTree fileTree = project.fileTree(buildDir);
		fileTree.include("package.json");
		fileTree.include(".npmrc");
		fileTree.include("**/*.ts");
		fileTree.exclude("**/*.d.ts");
		compileTypescriptTask.getInputs().files(fileTree);
		compileTypescriptTask.setWorkingDir(buildDir);
		compileTypescriptTask.getOutputs().dir(buildDir);
		try {
			Task processIntegrationTestResourcesTask = project.getTasks().getByName("processIntegrationTestResources");
			Task integrationCompileJavaTask = project.getTasks().getByName("compileIntegrationTestJava");
			Task assembleTask = project.getTasks().getByName("assemble");
			generateTask.dependsOn(assembleTask, integrationCompileJavaTask, processIntegrationTestResourcesTask);
		}
		catch (Exception e) {
			LOGGER.error("failed to setup dependencies, is integrationTest and testSet plugin properly setup", e);
		}

		ConfigurableFileTree assembleFileTree = project.fileTree(new File(buildDir, "src"));
		assembleFileTree.include("**/*.ts");
		assembleFileTree.include("**/*.js");
		assembleFileTree.include("**/*.js.map");

		Copy assembleSources = project.getTasks().create("assembleTypescript", Copy.class);
		assembleSources.from(assembleFileTree);
		assembleSources.from(new File(buildDir, "package.json"));
		assembleSources.into(distDir);
		assembleSources.dependsOn(compileTypescriptTask);

		TSGeneratorConfiguration config = new TSGeneratorConfiguration(project);
		project.getExtensions().add("typescriptGen", config);
	}
}
