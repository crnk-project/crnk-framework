package io.crnk.gen.asciidoc.capture;

import java.io.File;

public class AsciidocCaptureConfig {


    private File genDir;


    private int baseDepth;

    /**
     * location of generated documentation.
     */
    public File getGenDir() {
        return genDir;
    }

    public void setGenDir(File genDir) {
        this.genDir = genDir;
    }

    /**
     * base depth of sections, e.g. baseDepth=0 will lead to new chapters with "# title", whereas
     * baseDepth=2 will lead to "### title"
     */
    public int getBaseDepth() {
        return baseDepth;
    }

    public void setBaseDepth(int baseDepth) {
        this.baseDepth = baseDepth;
    }
}
