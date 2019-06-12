package io.crnk.gen.gradle;

import groovy.lang.Closure;
import io.crnk.gen.asciidoc.AsciidocGeneratorConfig;
import io.crnk.gen.asciidoc.internal.AsciidocGeneratorModule;
import io.crnk.gen.base.GeneratorConfig;
import io.crnk.gen.base.RuntimeConfiguration;
import io.crnk.gen.typescript.TSGeneratorConfig;
import io.crnk.gen.typescript.TSGeneratorModule;
import org.gradle.api.Project;

public class GeneratorExtension extends GeneratorConfig {

    private Project project;

    private Runnable initMethod;

    public GeneratorExtension(Project project, Runnable initMethod) {
        this.project = project;
        this.initMethod = initMethod;
        this.runtime = new RuntimeExtension(project);

        // reconfigure within extension, not in TSGeneratorConfig since the later is used also in forked mode
        setForked(true);
    }

    public void init() {
        initMethod.run();
    }

    public RuntimeConfiguration runtime(Closure<RuntimeConfiguration> closure) {
        return (RuntimeConfiguration) project.configure(getRuntime(), closure);
    }

    public TSGeneratorConfig getTypescript() {
        return (TSGeneratorConfig) getModuleConfig(TSGeneratorModule.NAME);
    }

    public TSGeneratorConfig typescript(Closure<TSGeneratorConfig> closure) {
        return (TSGeneratorConfig) project.configure(getTypescript(), closure);
    }

    public AsciidocGeneratorConfig getAsciidoc() {
        return (AsciidocGeneratorConfig) getModuleConfig(AsciidocGeneratorModule.NAME);
    }

    public AsciidocGeneratorConfig asciidoc(Closure<AsciidocGeneratorConfig> closure) {
        return (AsciidocGeneratorConfig) project.configure(getAsciidoc(), closure);
    }
}
