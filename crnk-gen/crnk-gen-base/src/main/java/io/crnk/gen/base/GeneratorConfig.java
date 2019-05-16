package io.crnk.gen.base;


import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class GeneratorConfig {

    protected RuntimeConfiguration runtime = new RuntimeConfiguration();

    private List<String> resourcePackages;

    private String metaResolverClassName = null;

    private boolean forked = false;

    @JsonIgnore
    private Map<String, GeneratorModuleConfigBase> moduleConfig = new HashMap<>();

    public GeneratorModuleConfigBase getModuleConfig(String name) {
        return Objects.requireNonNull(moduleConfig.get(name));
    }

    public Map<String, GeneratorModuleConfigBase> getModuleConfig() {
        return moduleConfig;
    }

    public RuntimeConfiguration getRuntime() {
        return runtime;
    }

    public String computeMetaResolverClassName() {
        if (metaResolverClassName != null) {
            return metaResolverClassName;
        }
        if (getResourcePackages() != null) {
            return "io.crnk.gen.runtime.reflections.ReflectionsMetaResolver";
        }
        if (runtime.getSpring().getConfiguration() != null) {
            return "io.crnk.gen.runtime.spring.SpringMetaResolver";
        }
        return "io.crnk.gen.runtime.cdi.CdiMetaResolver";
    }

    /**
     * @return scans the given package with reflection and generates those resources.
     * Provides a quick way of generation without having to start the full application (CDI, Spring, etc.)
     */
    public List<String> getResourcePackages() {
        return resourcePackages;
    }

    public void setResourcePackages(List<String> resourcePackages) {
        this.resourcePackages = resourcePackages;
    }

    public boolean isForked() {
        return forked;
    }

    public void setForked(boolean forked) {
        this.forked = forked;
    }

    public String getMetaResolverClassName() {
        return metaResolverClassName;
    }

    public void setMetaResolverClassName(String metaResolverClassName) {
        this.metaResolverClassName = metaResolverClassName;
    }
}
