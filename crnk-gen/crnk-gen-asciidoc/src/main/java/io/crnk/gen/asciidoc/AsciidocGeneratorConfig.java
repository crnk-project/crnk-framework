package io.crnk.gen.asciidoc;

import io.crnk.gen.base.GeneratorModuleConfigBase;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class AsciidocGeneratorConfig extends GeneratorModuleConfigBase {

    private Set<String> excludes = new HashSet<>();

    private File genDir = null;

    private File buildDir = null;

    private String title = "API Reference";

    private int baseDepth;

    /**
     * @return base depth of sections, e.g. baseDepth=0 will lead to new chapters with "# title", whereas baseDepth=2 will lead to "### title"
     */
    public int getBaseDepth() {
        return baseDepth;
    }

    public void setBaseDepth(int baseDepth) {
        this.baseDepth = baseDepth;
    }


    /**
     * @return location where the generated sources are placed.
     */
    public File getGenDir() {
        if (genDir == null) {
            return new File(buildDir, "generated/source/asciidoc/");
        }
        return genDir;
    }

    public void setGenDir(File genDir) {
        this.genDir = genDir;
    }

    /**
     * @return list of prefixes that should not be exported to generated
     */
    public Set<String> getExcludes() {
        return excludes;
    }

    public void setExcludes(Set<String> excludes) {
        this.excludes = excludes;
    }


    public File getBuildDir() {
        return buildDir;
    }

    public void setBuildDir(File buildDir) {
        this.buildDir = buildDir;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
