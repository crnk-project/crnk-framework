package io.crnk.gen.gradle;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import io.crnk.gen.asciidoc.internal.AsciidocGeneratorModule;
import io.crnk.gen.base.GeneratorConfig;
import io.crnk.gen.base.GeneratorModule;
import io.crnk.gen.gradle.task.ForkedGenerateTask;
import io.crnk.gen.gradle.task.GeneratorTaskContract;
import io.crnk.gen.gradle.task.InMemoryGeneratorTask;
import org.gradle.api.DefaultTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.tasks.TaskContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GeneratorPlugin implements Plugin<Project> {


	public static boolean APPLY_DOCLET_BY_DEFAULT = true;


	private static final Logger LOGGER = LoggerFactory.getLogger(GeneratorPlugin.class);

	private List<GeneratorModule> modules = new ArrayList<>();

	@Override
	public void apply(final Project project) {
		if(APPLY_DOCLET_BY_DEFAULT) {
			project.getPlugins().apply(DocletPlugin.class);
		}

		final Runnable initRunner = new Runnable() {

			private boolean initialized = false;

			@Override
			public void run() {
				if (!initialized) {
					initialized = true;
					init(project);

					for (GeneratorModule module : modules) {
						String taskName = getGenerateTaskName(module);
						Task task = project.getTasks().getByName(taskName);
						task.setEnabled(module.getConfig().isEnabled());
					}
				}
			}
		};
		final GeneratorExtension config = new GeneratorExtension(project, initRunner);

		ServiceLoader<GeneratorModule> loader = ServiceLoader.load(GeneratorModule.class);
		for (GeneratorModule module : loader) {
			LOGGER.debug("discovered generator module: {}", module.getName());
			modules.add(module);
			module.initDefaults(project.getBuildDir());
			config.getModuleConfig().put(module.getName(), module.getConfig());
		}
		LOGGER.debug("discovered {} modules", modules.size());

		project.getExtensions().add("crnkGen", config);
		project.afterEvaluate(project1 -> initRunner.run());
	}

	protected void init(Project project) {
		for (GeneratorModule module : modules) {
			setupCleanTask(project, module);
			Task generateTask = setupGenerateTask(project, module);

			if (module instanceof AsciidocGeneratorModule) {
				generateTask.dependsOn(DocletPlugin.TASK_NAME);
			}
		}
	}

	private Task setupCleanTask(Project project, GeneratorModule module) {
		Task task = project.getTasks().create(getCleanTaskName(module), DefaultTask.class);
		task.doFirst(task1 -> project.delete(module.getConfig().getGenDir()));
		return task;
	}

	private Task setupGenerateTask(Project project, GeneratorModule module) {
		GeneratorConfig config = project.getExtensions().getByType(GeneratorConfig.class);
		Class taskClass = config.isForked() ? ForkedGenerateTask.class : InMemoryGeneratorTask.class;
		Task task = project.getTasks().create(getGenerateTaskName(module), taskClass);

		((GeneratorTaskContract) task).setModule(module);

		setupRuntimeDependencies(project, task);

		// setup up-to-date checking
		String runtimeConfiguration = config.getRuntime().getConfiguration();
		Configuration compileConfiguration = project.getConfigurations().findByName(runtimeConfiguration);
		if (compileConfiguration != null) {
			task.getInputs().files(compileConfiguration.getFiles());
			task.getOutputs().dir(module.getGenDir());
		}

		return task;
	}

	private String getGenerateTaskName(GeneratorModule module) {
		return InMemoryGeneratorTask.NAME +
				Character.toUpperCase(module.getName().charAt(0)) + module.getName().substring(1);
	}

	private String getCleanTaskName(GeneratorModule module) {
		return "clean" +
				Character.toUpperCase(module.getName().charAt(0)) + module.getName().substring(1);
	}


	private void setupRuntimeDependencies(Project project, Task generateTask) {
		GeneratorConfig config = project.getExtensions().getByType(GeneratorConfig.class);
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
		}
	}
}
