package io.crnk.gen.typescript;

import com.moowork.gradle.node.npm.NpmInstallTask;
import io.crnk.gen.typescript.internal.TypescriptUtils;
import org.gradle.api.*;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.ConfigurableFileTree;
import org.gradle.api.tasks.Copy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class TSGeneratorPlugin implements Plugin<Project> {

	private static final Logger LOGGER = LoggerFactory.getLogger(TSGeneratorPlugin.class);

	@Override
	public void apply(final Project project) {
		final File sourcesDir = new File(project.getBuildDir(), "generated/source/typescript/");
		final File buildDir = new File(project.getBuildDir(), "npm_compile");
		final File distDir = new File(project.getBuildDir(), "npm");

		Configuration compileConfiguration = project.getConfigurations().getByName("compile");

		project.getTasks().create(PublishTypescriptStubsTask.NAME, PublishTypescriptStubsTask.class);

		final GenerateTypescriptTask generateTask = project.getTasks().create(GenerateTypescriptTask.NAME,
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
		} catch (UnknownTaskException e) {
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

		ConfigurableFileTree assembleFileTree = project.fileTree(new File(buildDir, "src"));
		assembleFileTree.include("**/*.ts");
		assembleFileTree.include("**/*.js");
		assembleFileTree.include("**/*.js.map");

		Copy assembleSources = project.getTasks().create("assembleTypescript", Copy.class);
		assembleSources.from(assembleFileTree);
		assembleSources.from(new File(buildDir, "package.json"));
		assembleSources.into(distDir);
		assembleSources.dependsOn(compileTypescriptTask);

		final TSGeneratorConfiguration config = new TSGeneratorConfiguration(project);
		project.getExtensions().add("typescriptGen", config);

		// setup dependency of generate task (configurable by extension)
		final Task assembleTask = project.getTasks().getByName("assemble");
		generateTask.dependsOn(assembleTask);
		project.afterEvaluate(new Action<Project>() {
			@Override
			public void execute(Project project) {
				String runtimeConfiguration = config.getRuntime().getConfiguration();
				String runtimeConfigurationFirstUpper = Character.toUpperCase(runtimeConfiguration.charAt(0)) + runtimeConfiguration.substring(1);

				Task processIntegrationTestResourcesTask = project.getTasks().getByName("process" + runtimeConfigurationFirstUpper + "Resources");
				Task integrationCompileJavaTask = project.getTasks().getByName("compile" + runtimeConfigurationFirstUpper + "Java");
				generateTask.dependsOn(integrationCompileJavaTask, processIntegrationTestResourcesTask);
			}
		});


	}
}
