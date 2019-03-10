package io.crnk.gen.typescript;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.gen.runtime.RuntimeClassLoaderFactory;
import org.gradle.api.Project;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ForkedGenerateTypescriptTask extends JavaExec {

	public static final String NAME = "generateTypescript";

	public ForkedGenerateTypescriptTask() {
		setGroup("generation");
		setDescription("generate Typescript stubs from a Crnk setup");
		setMain(ForkedGeneratorMain.class.getName());
	}

	@TaskAction
	public void exec() {
		initClassPath();
		initConfigFile();
		super.exec();
	}

	private void initConfigFile() {
		File configFile = new File(getProject().getBuildDir(), "crnk.gen.typescript.json");
		configFile.getParentFile().mkdirs();

		TSGeneratorConfig config = getConfig();
		setupDefaultConfig(config);
		ObjectMapper mapper = new ObjectMapper();
		try {
			mapper.writerFor(TSGeneratorConfig.class).writeValue(configFile, config);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
		setArgs(Arrays.asList(configFile.getAbsolutePath()));
	}


	private void initClassPath() {
		RuntimeClassLoaderFactory classLoaderFactory = new RuntimeClassLoaderFactory(getProject());

		Set<File> classpath = new HashSet<>();
		classpath.addAll(classLoaderFactory.getProjectLibraries());
		try {
			classpath.add(Paths.get(classLoaderFactory.getPluginUrl().toURI()).toFile());
		} catch (URISyntaxException e) {
			throw new IllegalStateException(e);
		}

		Project project = getProject();
		ConfigurableFileCollection files = project.files(classpath.toArray());
		setClasspath(files);
	}


	@OutputDirectory
	public File getOutputDirectory() {
		TSGeneratorConfig config = getConfig();
		return config.getGenDir();
	}


	protected void setupDefaultConfig(TSGeneratorConfig config) {
		String defaultVersion = getProject().getVersion().toString();
		if (config.getNpm().getPackageVersion() == null) {
			config.getNpm().setPackageVersion(defaultVersion);
		}
	}

	private TSGeneratorConfig getConfig() {
		Project project = getProject();
		return project.getExtensions().getByType(TSGeneratorConfig.class);
	}

}
