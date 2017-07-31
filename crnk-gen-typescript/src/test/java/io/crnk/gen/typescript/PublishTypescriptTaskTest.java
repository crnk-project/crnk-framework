package io.crnk.gen.typescript;

import io.crnk.gen.typescript.runtime.DummyInitialContextFactory;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import javax.naming.Context;
import java.io.File;
import java.io.IOException;

public class PublishTypescriptTaskTest {

	@Rule
	public TemporaryFolder testProjectDir = new TemporaryFolder();


	@Test
	public void checkTaskProperties() throws IOException {
		System.setProperty(Context.INITIAL_CONTEXT_FACTORY, DummyInitialContextFactory.class.getName());

		testProjectDir.newFolder("src", "main", "java");

		File outputDir = testProjectDir.getRoot();

		Project project = ProjectBuilder.builder().withName("crnk-gen-typescript-test").withProjectDir(outputDir).build();
		project.setVersion("0.0.1");
		project.getPluginManager().apply(JavaPlugin.class);
		project.getPluginManager().apply(TSGeneratorPlugin.class);

		PublishTypescriptStubsTask task = (PublishTypescriptStubsTask) project.getTasks().getByName("publishTypescript");
		Assert.assertEquals("publish", task.getGroup());
		Assert.assertNotNull(task.getDescription());
		Assert.assertFalse(task.getInputs().getFiles().isEmpty());
		Assert.assertFalse(task.getOutputs().getFiles().isEmpty());
	}
}
