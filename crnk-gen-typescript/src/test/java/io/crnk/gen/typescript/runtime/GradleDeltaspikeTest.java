package io.crnk.gen.typescript.runtime;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

@Ignore // classpath issues somewhere
public class GradleDeltaspikeTest {

	public TemporaryFolder testFolder = new TemporaryFolder();
	private File root;

	@Test
	public void test() throws IOException {
		testFolder.create();
		root = testFolder.getRoot();

		saveFile("test_build.gradle", "build.gradle");
		saveFile("test_settings.gradle", "settings.gradle");
		saveFile("META-INF/beans.xml", "src/main/resources/META-INF/beans.xml");
		saveFile("TestModuleProducer.template", "src/main/java/io/crnk/gen/typescript/TestModuleProducer.java");

		GradleRunner runner = GradleRunner.create();
		runner = runner.withPluginClasspath();

		// List<File> files = Arrays.asList(new File("C:\\projects\\oss\\crnk-framework\\crnk-gen-typescript\\build\\classes\\main"));


		runner = runner.withProjectDir(root);

		// TODO move to assembleTypescript once ngrx-json-api released
		runner = runner.withArguments("generateTypescript");


		BuildResult build = runner.build();
		System.out.println(build.getOutput());
	}

	private void saveFile(String resourceName, String targetPath) {
		File file = new File(root, targetPath);
		file.getParentFile().mkdirs();
		file.delete();

		try (FileOutputStream out = new FileOutputStream(file); InputStream in = getClass().getClassLoader().getResourceAsStream(resourceName)) {
			IOUtils.copy(in, out);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}
}
