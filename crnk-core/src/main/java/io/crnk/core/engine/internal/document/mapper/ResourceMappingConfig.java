package io.crnk.core.engine.internal.document.mapper;

public class ResourceMappingConfig {

    private boolean serializeLinks = true;

    private boolean ignoreDefaults = false;

    public boolean isIgnoreDefaults() {
        return ignoreDefaults;
    }

    public void setIgnoreDefaults(boolean ignoreDefaults) {
        this.ignoreDefaults = ignoreDefaults;
    }

    public boolean getSerializeLinks() {
        return serializeLinks;
    }

    public void setSerializeLinks(boolean serializeLinks) {
        this.serializeLinks = serializeLinks;
    }
}
