package io.crnk.gen.typescript;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TSGeneratorPlugin implements Plugin<Project> {

	private static final Logger LOGGER = LoggerFactory.getLogger(TSGeneratorPlugin.class);

	@Override
	public void apply(Project project) {
		GenerateTypescriptTask task = project.getTasks().create(GenerateTypescriptTask.NAME, GenerateTypescriptTask.class);

		try {
			Task processIntegrationTestResourcesTask = project.getTasks().getByName("processIntegrationTestResources");
			Task integrationCompileJavaTask = project.getTasks().getByName("compileIntegrationTestJava");
			Task assembleTask = project.getTasks().getByName("assemble");
			task.dependsOn(assembleTask, integrationCompileJavaTask, processIntegrationTestResourcesTask);
		}
		catch (Exception e) {
			LOGGER.error("failed to setup dependencies, is integrationTest and testSet plugin properly setup", e);
		}

		TSGeneratorConfiguration config = new TSGeneratorConfiguration();
		project.getExtensions().add("typescriptGen", config);
	}
}
