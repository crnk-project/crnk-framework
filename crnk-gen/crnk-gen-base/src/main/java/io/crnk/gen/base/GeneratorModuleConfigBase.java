package io.crnk.gen.base;

import java.io.File;

public abstract class GeneratorModuleConfigBase {

    private boolean enabled;


    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public abstract File getGenDir();
}
