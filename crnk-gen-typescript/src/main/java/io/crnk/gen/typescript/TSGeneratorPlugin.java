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
import org.gradle.api.tasks.TaskContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TSGeneratorPlugin implements Plugin<Project> {

	private static final Logger LOGGER = LoggerFactory.getLogger(TSGeneratorPlugin.class);


	@Override
	public void apply(final Project project) {

		final Runnable initRunner = new Runnable() {

			private boolean initialized = false;

			@Override
			public void run() {
				if (!initialized) {
					initialized = true;
					init(project);
				}
			}
		};

		final TSGeneratorConfig config = new TSGeneratorExtension(project, initRunner);
		project.getExtensions().add("typescriptGen", config);
		project.afterEvaluate(new Action<Project>() {
			@Override
			public void execute(Project project) {
				initRunner.run();
			}
		});
	}


	protected void init(Project project) {
		TSGeneratorConfig config = project.getExtensions().getByType(TSGeneratorConfig.class);
		Task generateTask = setupGenerateTask(project);
		if (config.getNpm().isPackagingEnabled()) {
			setupPackageTasks(project, generateTask);
		}
		setupRuntimeDependencies(project, generateTask);
	}

	private void setupRuntimeDependencies(Project project, Task generateTask) {
		TSGeneratorConfig config = project.getExtensions().getByType(TSGeneratorConfig.class);
		String runtimeConfiguration = config.getRuntime().getConfiguration();
		if (runtimeConfiguration != null) {
			String runtimeConfigurationFirstUpper =
					Character.toUpperCase(runtimeConfiguration.charAt(0)) + runtimeConfiguration.substring(1);

			// make sure applications is compiled in order to startup and extract meta information
			String processResourcesName = "process" + runtimeConfigurationFirstUpper + "Resources";
			String compileJavaName = "compile" + runtimeConfigurationFirstUpper + "Java";
			TaskContainer tasks = project.getTasks();
			Task processResourceTask = tasks.findByName(processResourcesName);
			Task compileJavaTask = tasks.findByName(compileJavaName);
			if (processResourceTask != null) {
				generateTask.dependsOn(processResourceTask);
			}
			if (compileJavaTask != null) {
				generateTask.dependsOn(compileJavaTask, compileJavaTask);
			}

			// setup up-to-date checking
			Configuration compileConfiguration = project.getConfigurations().findByName(runtimeConfiguration);
			if (compileConfiguration != null) {
				generateTask.getInputs().file(compileConfiguration.getFiles());
				generateTask.getOutputs().dir(config.getGenDir());
			}
		}
	}

	private File getNpmOutputDir(Project project) {
		TSGeneratorConfig config = project.getExtensions().getByType(TSGeneratorConfig.class);
		File typescriptGenDir = config.getNpm().getOutputDir();
		if (typescriptGenDir == null) {
			return new File(project.getBuildDir(), "npm");
		}
		return typescriptGenDir;
	}

	private Task setupGenerateTask(Project project) {
		TSGeneratorConfig config = project.getExtensions().getByType(TSGeneratorConfig.class);
		Class taskClass = config.isForked() ? ForkedGenerateTypescriptTask.class : GenerateTypescriptTask.class;
		return project.getTasks().create(GenerateTypescriptTask.NAME, taskClass);
	}

	void setupPackageTasks(Project project, Task generateTask) {
		final File buildDir = new File(project.getBuildDir(), "npm_compile");
		final File distDir = getNpmOutputDir(project);

		project.getTasks().create(PublishTypescriptStubsTask.NAME, PublishTypescriptStubsTask.class);
		TSGeneratorConfig config = project.getExtensions().getByType(TSGeneratorConfig.class);

		Copy copySources = project.getTasks().create("processTypescript", Copy.class);
		copySources.from(config.getGenDir());
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
					buildDir.mkdirs();
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

		ConfigurableFileTree assembleFileTree = project.fileTree(new File(buildDir, "src"));
		assembleFileTree.include("**/*.ts");
		assembleFileTree.include("**/*.js");
		assembleFileTree.include("**/*.js.map");

		Copy assembleSources = project.getTasks().create("assembleTypescript", Copy.class);
		assembleSources.from(assembleFileTree);
		assembleSources.from(new File(buildDir, "package.json"));
		assembleSources.into(distDir);
		assembleSources.dependsOn(compileTypescriptTask);
	}
}
