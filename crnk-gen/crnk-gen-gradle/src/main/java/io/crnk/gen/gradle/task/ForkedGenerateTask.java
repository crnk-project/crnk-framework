package io.crnk.gen.gradle.task;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.gen.base.GeneratorConfig;
import io.crnk.gen.base.GeneratorModule;
import io.crnk.gen.gradle.internal.RuntimeClassLoaderFactory;
import org.gradle.api.Project;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ForkedGenerateTask extends JavaExec implements GeneratorTaskContract {

    private GeneratorModule module;

    public ForkedGenerateTask() {
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
        File mainFile = new File(getProject().getBuildDir(), "crnk.gen.config.main.json");
        File moduleFile = new File(getProject().getBuildDir(), "crnk.gen.config.module.json");
        moduleFile.getParentFile().mkdirs();

        GeneratorConfig config = getConfig();
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writerFor(GeneratorConfig.class).writeValue(mainFile, config);
            mapper.writerFor(module.getConfig().getClass()).writeValue(moduleFile, module.getConfig());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        setArgs(Arrays.asList(mainFile.getAbsolutePath(), moduleFile.getAbsolutePath(), module.getClass().getName()));
    }


    private void initClassPath() {
        RuntimeClassLoaderFactory classLoaderFactory = new RuntimeClassLoaderFactory(getProject(), module);

        Set<File> classpath = new HashSet<>();
        classpath.addAll(classLoaderFactory.getProjectLibraries());
        try {
            for (URL pluginUrl : classLoaderFactory.getPluginUrls()) {
                classpath.add(Paths.get(pluginUrl.toURI()).toFile());
            }
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }

        Project project = getProject();
        ConfigurableFileCollection files = project.files(classpath.toArray());
        setClasspath(files);
    }

    @OutputDirectory
    public File getOutputDirectory() {
        return module.getGenDir();
    }

    private GeneratorConfig getConfig() {
        Project project = getProject();
        return project.getExtensions().getByType(GeneratorConfig.class);
    }

    @Override
    public void setModule(GeneratorModule module) {
        this.module = module;
    }
}
